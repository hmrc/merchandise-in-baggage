package uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori

import play.api.libs.json.{Format, Json}

case class CheckEoriAddress(streetAndNumber: String, cityName: String, postcode: String)

object CheckEoriAddress {
  implicit val format: Format[CheckEoriAddress] = Json.format[CheckEoriAddress]
}