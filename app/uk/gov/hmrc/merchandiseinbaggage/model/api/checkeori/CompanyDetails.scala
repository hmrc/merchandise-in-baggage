package uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori

import play.api.libs.json.{Format, Json}

case class CompanyDetails(traderName: String, address: CheckEoriAddress)

object CompanyDetails {
  implicit val format: Format[CompanyDetails] = Json.format[CompanyDetails]
}