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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.mvc.ControllerComponents
import uk.gov.hmrc.merchandiseinbaggage.connectors.ExchangeRateConnector
import uk.gov.hmrc.merchandiseinbaggage.stubs.ExchangeRateStub.givenExchangeServer
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData, WireMock}
import play.api.libs.json.Json
import play.api.test.Helpers.{contentAsJson, status}
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.model.api.ExchangeRateURL

import scala.concurrent.ExecutionContext.Implicits.global

class ExchangeRateControllerSpec extends BaseSpecWithApplication with CoreTestData with WireMock {

  val connector: ControllerComponents = injector.instanceOf[ControllerComponents]
  val rates: ExchangeRateConnector = injector.instanceOf[ExchangeRateConnector]

  "return with month page URI" in {
    givenExchangeServer()

    val controller = new ExchangeRateController(connector, rates)

    val result = controller.url.apply(fakeRequest)

    status(result) mustBe 200
    contentAsJson(result) mustBe Json.toJson(ExchangeRateURL(
      "https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/974580/exrates-monthly-0421.csv/preview"))

  }

}
