/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage

import java.time.LocalDate

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import uk.gov.hmrc.merchandiseinbaggage.config.CurrencyConversionConfiguration

trait CurrencyConversionStub extends BaseSpec with BaseSpecWithWireMock with CurrencyConversionConfiguration {

  private val todayDate: LocalDate = LocalDate.now

  def getCurrencyConversionStub(currency: String): StubMapping =
    currencyConversionMockServer.stubFor(get(urlEqualTo(s"${currencyConversionPostFix(todayDate, currency)}"))
      .willReturn(okJson(responseTemplate)))

  val responseTemplate =
    s"""[
       |    {
       |        "startDate": "$todayDate",
       |        "endDate": "$todayDate",
       |        "currencyCode": "USD",
       |        "rate": "1.3064"
       |    }
       |]""".stripMargin

}
