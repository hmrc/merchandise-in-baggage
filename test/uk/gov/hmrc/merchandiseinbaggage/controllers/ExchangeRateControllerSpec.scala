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
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData, WireMock}
import play.api.libs.json.Json
import play.api.test.Helpers.{contentAsJson, status}
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.model.api.ExchangeRateURL
import uk.gov.hmrc.merchandiseinbaggage.stubs.ExchangeRateStub

import scala.concurrent.ExecutionContext.Implicits.global

class ExchangeRateControllerSpec extends BaseSpecWithApplication with CoreTestData with WireMock {

  val wiremockServer = s"http://localhost:${WireMock.port}"

  val connector: ControllerComponents = injector.instanceOf[ControllerComponents]

  "return with month page URI" in {
    ExchangeRateStub.givenExchangeServer()

    val rates = new ExchangeRateConnector() {
      override def calcYearPage(year: Int): String =
        s"$wiremockServer/government/publications/hmrc-exchange-rates-for-2021-monthly"
    }

    val controller = new ExchangeRateController(connector, rates)
    val result = controller.url.apply(fakeRequest)

    status(result) mustBe 200
    contentAsJson(result) mustBe Json.toJson(
      ExchangeRateURL(s"$wiremockServer/government/uploads/system/uploads/attachment_data/file/974580/exrates-monthly-0421.csv/preview"))
  }

  "unable to find month page then returns year URI" in {
    val rates = new ExchangeRateConnector() {
      override def calcYearPage(year: Int): String =
        s"$wiremockServer/government/publications/hmrc-exchange-rates-for-2020-monthly"
    }

    val controller = new ExchangeRateController(connector, rates)
    val result = controller.url.apply(fakeRequest)

    status(result) mustBe 200
    contentAsJson(result) mustBe Json.toJson(
      ExchangeRateURL(s"$wiremockServer/government/publications/hmrc-exchange-rates-for-2020-monthly"))
  }
}
