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

import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.config.{ExchangeRateConf, ExchangeRateConfiguration}
import uk.gov.hmrc.merchandiseinbaggage.stubs.ExchangeRateStub.givenExchangeServer
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, WireMock}

import scala.concurrent.ExecutionContext.Implicits.global

class ExchangeRateConnectorSpec extends BaseSpecWithApplication with WireMock {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val httpClient = app.injector.instanceOf[HttpClient]

  trait WireMockExchangeRateConfiguration extends ExchangeRateConfiguration {
    override lazy val exchangeRateConf: ExchangeRateConf = ExchangeRateConf("http", "localhost", 17777)
  }

  val exchange = new ExchangeRateConnector(httpClient) with WireMockExchangeRateConfiguration

  "return with month page URI" in {
    givenExchangeServer()

    whenReady(exchange.monthlyURL(2021)) { result =>
      result mustBe "https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/974580/exrates-monthly-0421.csv/preview"
    }
  }

  "unable to find month page then returns year URI" in {
    givenExchangeServer()

    whenReady(exchange.monthlyURL(2020)) { result =>
      result mustBe s"${exchange.exchangeRateConf.exchangeRateUrl}/government/publications/hmrc-exchange-rates-for-2020-monthly"
    }
  }

  "Badly formated month page then returns year URI" in {
    givenExchangeServer()

    whenReady(exchange.monthlyURL(2019)) { result =>
      result mustBe s"${exchange.exchangeRateConf.exchangeRateUrl}/government/publications/hmrc-exchange-rates-for-2019-monthly"
    }
  }

}
