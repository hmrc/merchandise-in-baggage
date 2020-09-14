/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.mvc.{AnyContent, Request, WrappedRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationRequest

case class RequestWithDeclaration[A](request: Request[A], paymentRequest: DeclarationRequest) extends WrappedRequest(request)
object RequestWithDeclaration {
  def apply[A]()(implicit request: Request[AnyContent]): Option[RequestWithDeclaration[_]] =
    for {
      parsed         <- request.body.asJson
      paymentRequest <- parsed.asOpt[DeclarationRequest]
    } yield RequestWithDeclaration(request, paymentRequest)
}
