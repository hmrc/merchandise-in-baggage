package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class EoriNumberSpec extends BaseSpec with CoreTestData {

  "serialise/deserialise from/to json as Eori" in {

    Json.parse(aSuccessCheckResponse).as[CheckResponse] mustBe aCheckResponse
  }
}
