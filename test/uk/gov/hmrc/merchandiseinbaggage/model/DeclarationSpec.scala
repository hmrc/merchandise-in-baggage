/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model

import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.BaseSpec

class DeclarationSpec extends BaseSpec {

  "Serialise/Deserialise from/to json to Declaration" in {
    val declaration = Declaration(
      Name("Valentino Rossi"),
      Amount(111),
      Reference("some ref")
    )

    val actual = Json.toJson(declaration).toString

    Json.toJson(declaration) mustBe Json.parse(actual)
  }
}
