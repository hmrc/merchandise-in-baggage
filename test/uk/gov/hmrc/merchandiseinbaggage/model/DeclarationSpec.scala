/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model

import java.time.LocalDateTime

import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.BaseSpec

class DeclarationSpec extends BaseSpec {

  "Serialise/Deserialise from/to json to Declaration" in {
    val declaration = Declaration(
      "1234",
      Name("Valentino Rossi"),
      Amount(111),
      Reference("some ref"),
      LocalDateTime.now
    )

    val actual = Json.toJson(declaration).toString

    Json.toJson(declaration) mustBe Json.parse(actual)
  }
}
