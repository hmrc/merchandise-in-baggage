/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggage.model.api.PaymentRequest
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.service.PaymentService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class PaymentController @Inject()(mcc: MessagesControllerComponents,
                                  declarationRepository: DeclarationRepository)(implicit val ec: ExecutionContext)
  extends BackendController(mcc) with PaymentService {

  def onPayment(): Action[AnyContent] = Action(parse.default).async { implicit request  =>
    RequestWithPayment().map(rwp =>
      persistDeclaration(declarationRepository.insert, rwp.paymentRequest).map { dec =>
        Created(Json.toJson(dec.declarationId))
      }
    ).getOrElse(Future.successful(InternalServerError("Invalid Request")))
  }
}

case class RequestWithPayment[A](request: Request[A], paymentRequest: PaymentRequest) extends WrappedRequest(request)
object RequestWithPayment {
  def apply[A]()(implicit request: Request[AnyContent], ec: ExecutionContext): Option[RequestWithPayment[_]] =
    for {
      parsed         <- request.body.asJson
      paymentRequest <- parsed.asOpt[PaymentRequest]
    } yield RequestWithPayment(request, paymentRequest)
}
