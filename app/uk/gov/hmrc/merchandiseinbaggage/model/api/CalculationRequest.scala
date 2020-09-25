/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{AmountInPence, Currency}

//TODO currency is just a string for now - waiting for conversion integration
case class CalculationRequest(currency: String, amount: AmountInPence)

object CalculationRequest {
  implicit val format: Format[CalculationRequest] = Json.format
}
