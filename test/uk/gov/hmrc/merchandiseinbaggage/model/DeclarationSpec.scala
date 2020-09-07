/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model

import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class DeclarationSpec extends BaseSpec with CoreTestData {

  "Serialise/Deserialise from/to json to Declaration" in {
    val declaration = aDeclaration
    val actual = Json.toJson(declaration).toString

    Json.toJson(declaration) mustBe Json.parse(actual)
  }
}
