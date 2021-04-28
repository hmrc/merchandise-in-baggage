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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import uk.gov.hmrc.merchandiseinbaggage.stubs.ExchangeRateStub
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, WireMock}
import java.time.LocalDate

class ExchangeRateConnectorSpec extends BaseSpecWithApplication with WireMock {

  "return with month page URI" in {

    ExchangeRateStub.givenExchangeServer()

    val exchange = new ExchangeLinkLoaderImpl()

    val server = s"http://localhost:${WireMock.port}"

    val monthYear = LocalDate.of(2021, 4, 5)
    val yearUrl = s"$server/government/publications/hmrc-exchange-rates-for-2021-monthly"
    whenReady(exchange.getMonthlyUrl(yearUrl, monthYear)) { result =>
      result mustBe s"$server/government/uploads/system/uploads/attachment_data/file/974580/exrates-monthly-0421.csv/preview"
    }
  }

  "unable to find month page then returns year URI" in {
    val exchange = new ExchangeLinkLoaderImpl() {
      override def getPage(yearlyUrl: String): Document =
        throw new Exception("Something odd")
    }

    val monthYear = LocalDate.of(2020, 4, 5)
    val yearUrl = "https://www.gov.uk/government/publications/hmrc-exchange-rates-for-2020-monthly"
    whenReady(exchange.getMonthlyUrl(yearUrl, monthYear)) { result =>
      result mustBe yearUrl
    }
  }

  "Badly formated month page then returns year URI" in {
    val exchange = new ExchangeLinkLoaderImpl() {
      override def getPage(yearlyUrl: String): Document =
        Jsoup.parse(
          """<body><h1 class="gem-c-heading ">Documents</h2><a href="http://something.com/government/uploads/system/uploads/attachment_data/file/974580/exrates-monthly-0421.csv/preview">View online</a>
            |""".stripMargin)
    }

    val monthYear = LocalDate.of(2020, 4, 5)
    val yearUrl = "https://www.gov.uk/government/publications/hmrc-exchange-rates-for-2020-monthly"
    whenReady(exchange.getMonthlyUrl(yearUrl, monthYear)) { result =>
      result mustBe yearUrl
    }
  }

}
