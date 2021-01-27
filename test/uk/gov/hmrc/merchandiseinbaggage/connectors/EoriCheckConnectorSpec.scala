/*
 * Copyright 2021 HM Revenue & Customs
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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.Eori
import uk.gov.hmrc.merchandiseinbaggage.stubs.EoriCheckStub._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, WireMock}

import scala.concurrent.ExecutionContext.Implicits.global

class EoriCheckConnectorSpec extends BaseSpecWithApplication with WireMock {

  val client = app.injector.instanceOf[EoriCheckConnector]
  implicit val hc = HeaderCarrier()

  "send a declaration to backend to be persisted" in {
    val eori = Eori("GB1234")
    givenEoriCheck(eori)

    client.checkEori(eori).futureValue.status mustBe 200
  }
}
