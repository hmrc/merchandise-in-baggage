/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import cats.instances.future._
import play.api.Logger
import play.api.i18n.Messages
import play.api.libs.json.Json.{prettyPrint, toJson}
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationRequest, MibReference}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationId, DeclarationNotFound}
import uk.gov.hmrc.merchandiseinbaggage.service.DeclarationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DeclarationController @Inject()(declarationService: DeclarationService,
                                      mcc: MessagesControllerComponents)(implicit val ec: ExecutionContext)
  extends BackendController(mcc) {

  implicit def messages(implicit request: Request[_]): Messages = mcc.messagesApi.preferred(request)

  private val logger = Logger(this.getClass)

  def onDeclarations(): Action[DeclarationRequest] = Action(parse.json[DeclarationRequest]).async { implicit request =>
    val declarationRequest = request.body
    val obfuscatedLogText = prettyPrint(toJson(declarationRequest.obfuscated))

    logger.info(s"Received declaration request [$obfuscatedLogText]")

    declarationService.persistDeclaration(declarationRequest).map { dec =>
      logger.info(s"Persisted declaration request for session id [${declarationRequest.sessionId}]")
      Created(toJson(dec.declarationId))
    }
  }

  def onRetrieve(declarationId: String): Action[AnyContent] = Action.async {
    logger.info(s"Received retrieve request for declarationId [$declarationId]")

    declarationService.findByDeclarationId(DeclarationId(declarationId)).fold(
      {
        case DeclarationNotFound =>
          logger.warn(s"DeclarationId [$declarationId] not found")
          NotFound
        case e =>
          logger.error(s"Error for declarationId [$declarationId] - [$e]]")
          InternalServerError("Something went wrong")
      },
      foundDeclaration => {
        logger.info(s"Found [${prettyPrint(toJson(foundDeclaration.obfuscated))}]")
        Ok(toJson(foundDeclaration))
      }
    )
  }

  def sendEmails(declarationId: String): Action[AnyContent] = Action.async { implicit request =>
    declarationService.sendEmails(DeclarationId(declarationId)).fold(
      {
        case DeclarationNotFound =>
          logger.warn(s"DeclarationId [$declarationId] not found")
          NotFound
        case e =>
          logger.error(s"Error for declarationId [$declarationId] - [$e]]")
          InternalServerError(s"${e} during sending emails")
      },
      _ => {
        Status(ACCEPTED)
      }
    )
  }

  def paymentSuccessCallback(mibRef: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"got the payment callback for reference: $mibRef")

    val result = for {
      foundDeclaration <- declarationService.findByMibReference(MibReference(mibRef))
      updatedDeclaration <- declarationService.upsertDeclaration(foundDeclaration.copy(paymentSuccess = Some(true)))
      emailResponse <- declarationService.sendEmails(updatedDeclaration.declarationId)
    } yield {
      emailResponse
    }

    result.fold(
      {
        case DeclarationNotFound =>
          logger.warn(s"Declaration with MibReference [$mibRef] not found")
          NotFound
        case e =>
          logger.error(s"Error for MibReference [$mibRef] - [$e]]")
          InternalServerError("Something went wrong")
      },
      _ => Ok
    )
  }
}
