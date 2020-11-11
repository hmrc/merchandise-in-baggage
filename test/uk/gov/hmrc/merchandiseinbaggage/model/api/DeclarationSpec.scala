package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.libs.json.Json.{parse, toJson}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class DeclarationSpec extends BaseSpec with CoreTestData {

  "serialise and de-serialise" in {
    parse(toJson(aDeclaration).toString()).validate[Declaration].get mustBe aDeclaration
  }
}
