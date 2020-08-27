/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Amount, ChargeReference, CsgTpsProviderId, TraderName}

case class PaymentRequest(traderName: TraderName, amount: Amount, csgTpsProviderId: CsgTpsProviderId, chargeReference: ChargeReference)
object PaymentRequest {
  implicit val format: Format[PaymentRequest] = Json.format
}
