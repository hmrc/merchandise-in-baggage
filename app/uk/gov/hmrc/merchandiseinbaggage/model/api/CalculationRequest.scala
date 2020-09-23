/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{AmountInPence, Currency}

case class CalculationRequest(currency: Currency, amount: AmountInPence)

object CalculationRequest {
  implicit val format: Format[CalculationRequest] = Json.format
}
