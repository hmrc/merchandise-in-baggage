/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationId

case class DeclarationIdResponse(id: DeclarationId)
object DeclarationIdResponse {
  implicit val format: Format[DeclarationIdResponse] = Json.format
}

