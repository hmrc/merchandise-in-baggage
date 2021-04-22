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

package uk.gov.hmrc.merchandiseinbaggage.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, anyUrl, get, urlMatching, urlPathEqualTo, urlPathMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import scala.io.{BufferedSource, Source}

object ExchangeRateStub {

  val goodSource: BufferedSource = Source.fromURL(getClass.getResource("/exchangeRateData/hmrc-exchange-rates-for-2021-monthly.html"))
  val incorrectSource: BufferedSource = Source.fromURL(getClass.getResource("/exchangeRateData/incorrect-format.html"))

  def givenExchangeServer()(implicit server: WireMockServer): StubMapping = {

    // Correct format
    server.stubFor(
      get(urlPathMatching("/government/publications/hmrc-exchange-rates-for-2021-monthly"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(goodSource.mkString)))

    // Missing file
    server.stubFor(
      get(urlMatching("/government/publications/hmrc-exchange-rates-for-2020-monthly"))
        .willReturn(aResponse()
          .withStatus(400)))

    // Correct format
    server.stubFor(
      get(urlPathMatching("/government/publications/hmrc-exchange-rates-for-2019-monthly"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(incorrectSource.mkString)))

  }
}
