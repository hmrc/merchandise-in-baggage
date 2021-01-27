package uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori

import play.api.libs.json.{Json, OFormat}

case class CheckResponse(eori: String, valid: Boolean, companyDetails: Option[CompanyDetails])

object CheckResponse {
  implicit val checkResponseFormat: OFormat[CheckResponse] = Json.format[CheckResponse]
}