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

import play.api.mvc.{AnyContent, Request, WrappedRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationRequest, PaymentStatusRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.core.PaymentStatus

case class RequestWithDeclaration[A](request: Request[A], paymentRequest: DeclarationRequest) extends WrappedRequest(request)
object RequestWithDeclaration {
  def apply[A]()(implicit request: Request[AnyContent]): Option[RequestWithDeclaration[_]] =
    for {
      parsed         <- request.body.asJson
      paymentRequest <- parsed.asOpt[DeclarationRequest]
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