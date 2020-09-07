/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.mvc.{AnyContent, Request, WrappedRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{PaymentRequest, PaymentStatusRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.core.PaymentStatus

case class RequestWithDeclaration[A](request: Request[A], paymentRequest: PaymentRequest) extends WrappedRequest(request)
object RequestWithDeclaration {
  def apply[A]()(implicit request: Request[AnyContent]): Option[RequestWithDeclaration[_]] =
    for {
      parsed         <- request.body.asJson
      paymentRequest <- parsed.asOpt[PaymentRequest]
    } yield RequestWithDeclaration(request, paymentRequest)
}

case class RequestWithPaymentStatus[A](request: Request[A], paymentStatus: PaymentStatus) extends WrappedRequest(request)
object RequestWithPaymentStatus {
  def apply[A]()(implicit request: Request[AnyContent]): Option[RequestWithPaymentStatus[_]] =
    for {
      parsed         <- request.body.asJson
      statusRequest  <- parsed.asOpt[PaymentStatusRequest]
    } yield RequestWithPaymentStatus(request, statusRequest.status)
}