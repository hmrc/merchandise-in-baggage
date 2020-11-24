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
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json.{prettyPrint, toJson}
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationId, DeclarationNotFound}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.service.DeclarationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class DeclarationController @Inject()(mcc: MessagesControllerComponents,
                                      declarationRepository: DeclarationRepository)(implicit val ec: ExecutionContext)
  extends BackendController(mcc) with DeclarationService {

  private val logger = Logger(this.getClass)

  def onDeclarations(): Action[DeclarationRequest] = Action(parse.json[DeclarationRequest]).async { implicit request =>
    val declarationRequest = request.body
    val obfuscatedLogText = prettyPrint(toJson(declarationRequest.obfuscated))

    logger.info(s"Received declaration request [$obfuscatedLogText]")

    persistDeclaration(declarationRepository.insert, declarationRequest).map { dec =>
      logger.info(s"Persisted declaration request for session id [${declarationRequest.sessionId}]")
      Created(toJson(dec.declarationId))
    }
  }

  def onRetrieve(declarationId: String): Action[AnyContent] = Action.async {
    logger.info(s"Received retrieve request for declarationId [$declarationId]")

    findByDeclarationId(declarationRepository.findByDeclarationId, DeclarationId(declarationId)).fold(
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
}
