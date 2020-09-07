/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.api

import java.util.UUID

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.core._

case class DeclarationRequest(traderName: TraderName, amount: AmountInPence, csgTpsProviderId: CsgTpsProviderId, chargeReference: ChargeReference)
object DeclarationRequest {
  implicit val format: Format[DeclarationRequest] = Json.format

  implicit class ToDeclaration(paymentRequest: DeclarationRequest) {
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
