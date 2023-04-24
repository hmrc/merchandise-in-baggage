/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.merchandiseinbaggage.connectors

import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.Eori
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse
import uk.gov.hmrc.merchandiseinbaggage.stubs.EoriCheckStub._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, WireMock}

class EoriCheckConnectorSpec extends BaseSpecWithApplication with WireMock {

  val client      = app.injector.instanceOf[EoriCheckConnector]
  implicit val hc = HeaderCarrier()

  "handle a valid EORI by calling the API" in {
    val eori = Eori("GB1234")
    givenEoriCheck(eori, Json.parse(aSuccessCheckResponse).as[List[CheckResponse]])

    client.checkEori(eori).futureValue mustBe aCheckResponse :: Nil
  }

  "handle an invalid EORI by calling the API" in {
    val eori    = Eori("GB1234")
    val invalid = Json.parse("""[{"eori": "GB1234","valid": false}]""".stripMargin).as[List[CheckResponse]]
    givenEoriCheck(eori, invalid)

    client.checkEori(eori).futureValue mustBe invalid
  }
}
