/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationIdResponse, PaymentRequest, PaymentStatusRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationIdResponse._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationId, InvalidPaymentStatus, PaymentStatus}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.service.PaymentService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import cats.instances.future._

import scala.concurrent.{ExecutionContext, Future}

class PaymentController @Inject()(mcc: MessagesControllerComponents,
                                  declarationRepository: DeclarationRepository)(implicit val ec: ExecutionContext)
  extends BackendController(mcc) with PaymentService {

  def onPayments(): Action[AnyContent] = Action(parse.default).async { implicit request  =>
    RequestWithPayment().map(rwp =>
      persistDeclaration(declarationRepository.insert, rwp.paymentRequest).map { dec =>
        Created(Json.toJson(DeclarationIdResponse(dec.declarationId)))
      }
    ).getOrElse(Future.successful(InternalServerError("Invalid Request")))
  }

  def onUpdate(id: String): Action[AnyContent] = Action(parse.default).async { implicit request  =>
    RequestWithPaymentStatus()
      .map(requestWithStatus =>
      updatePaymentStatus(declarationRepository.findByDeclarationId, declarationRepository.updateStatus,
        DeclarationId(id), requestWithStatus.paymentStatus).fold ({
        case InvalidPaymentStatus => BadRequest
      }, (_ => NoContent))
    ).getOrElse(Future.successful(InternalServerError("Invalid Request")))
  }
}


//TODO duplication to be removed
case class RequestWithPayment[A](request: Request[A], paymentRequest: PaymentRequest) extends WrappedRequest(request)
object RequestWithPayment {
  def apply[A]()(implicit request: Request[AnyContent]): Option[RequestWithPayment[_]] =
    for {
      parsed         <- request.body.asJson
      paymentRequest <- parsed.asOpt[PaymentRequest]
    } yield RequestWithPayment(request, paymentRequest)
}

case class RequestWithPaymentStatus[A](request: Request[A], paymentStatus: PaymentStatus) extends WrappedRequest(request)
object RequestWithPaymentStatus {
  def apply[A]()(implicit request: Request[AnyContent]): Option[RequestWithPaymentStatus[_]] =
    for {
      parsed         <- request.body.asJson
      statusRequest  <- parsed.asOpt[PaymentStatusRequest]
    } yield RequestWithPaymentStatus(request, statusRequest.status)
}
