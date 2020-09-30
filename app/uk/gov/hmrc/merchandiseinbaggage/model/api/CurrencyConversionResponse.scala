/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.libs.json.{Format, Json}

case class CurrencyConversionResponse(currencyCode: String, rate: Option[String])

object CurrencyConversionResponse {
  implicit val format: Format[CurrencyConversionResponse] = Json.format
}
