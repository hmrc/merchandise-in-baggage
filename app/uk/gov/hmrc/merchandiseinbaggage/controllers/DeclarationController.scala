/*
 * Copyright 2020 HM Revenue & Customs
 *
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

  def onRetrieve(declarationId: String): Action[AnyContent] = Action(parse.default).async { implicit request  =>
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
