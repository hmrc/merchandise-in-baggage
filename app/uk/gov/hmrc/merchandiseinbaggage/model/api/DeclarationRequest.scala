/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.core._

case class DeclarationRequest(traderName: TraderName, amount: AmountInPence, csgTpsProviderId: CsgTpsProviderId, chargeReference: ChargeReference)
object DeclarationRequest {
  implicit val format: Format[DeclarationRequest] = Json.format

  implicit class ToDeclaration(paymentRequest: DeclarationRequest) {
    def toDeclaration: Declaration = {
      import paymentRequest._
      Declaration(traderName, amount, csgTpsProviderId, chargeReference)
    }
  }
}
