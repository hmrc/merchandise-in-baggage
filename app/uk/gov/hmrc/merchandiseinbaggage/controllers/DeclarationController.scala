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
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationIdResponse
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationIdResponse._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationId, DeclarationNotFound, InvalidPaymentStatus}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.service.DeclarationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class DeclarationController @Inject()(mcc: MessagesControllerComponents,
                                      declarationRepository: DeclarationRepository)(implicit val ec: ExecutionContext)
  extends BackendController(mcc) with DeclarationService {

  def onDeclarations(): Action[AnyContent] = Action(parse.default).async { implicit request  =>
    RequestWithDeclaration().map(rwp =>
      persistDeclaration(declarationRepository.insert, rwp.paymentRequest).map { dec =>
        Created(Json.toJson(DeclarationIdResponse(dec.declarationId)))
      }
    ).getOrElse(Future.successful(InternalServerError("Invalid Request")))
  }

  def onRetrieve(declarationId: String): Action[AnyContent] = Action(parse.default).async {
    findByDeclarationId(declarationRepository.findByDeclarationId, DeclarationId(declarationId)) fold ({
      case DeclarationNotFound => NotFound
      case _                   => InternalServerError("Something went wrong")
    }, foundDeclaration => Ok(Json.toJson(foundDeclaration)))
  }

  def onUpdate(id: String): Action[AnyContent] = Action(parse.default).async { implicit request  =>
    RequestWithPaymentStatus()
      .map(requestWithStatus =>
      updatePaymentStatus(declarationRepository.findByDeclarationId, declarationRepository.updateStatus,
        DeclarationId(id), requestWithStatus.paymentStatus) fold ({
        case InvalidPaymentStatus => BadRequest
        case DeclarationNotFound  => NotFound
        case _                    => BadRequest
      }, _ => NoContent)
    ).getOrElse(Future.successful(InternalServerError("Invalid Request")))
  }
}
