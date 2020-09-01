/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.api

import java.util.UUID

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.core._

case class PaymentRequest(traderName: TraderName, amount: Amount, csgTpsProviderId: CsgTpsProviderId, chargeReference: ChargeReference)
object PaymentRequest {
  implicit val format: Format[PaymentRequest] = Json.format

  implicit class ToDeclaration(paymentRequest: PaymentRequest) {
    def toDeclarationInInitialState: Declaration = {
      import paymentRequest._
      Declaration(DeclarationId(UUID.randomUUID().toString), traderName, amount, csgTpsProviderId,
        chargeReference, Outstanding, None, None)
    }
  }
}

case class PaymentStatusRequest(status: PaymentStatus)
object PaymentStatusRequest {
  implicit val format: Format[PaymentStatusRequest] = Json.format
}
