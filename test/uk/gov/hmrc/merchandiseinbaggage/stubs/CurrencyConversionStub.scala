/*
 * Copyright 2024 HM Revenue & Customs
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

import java.time.LocalDate.now
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlMatching}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.model.api.ConversionRatePeriod

object CurrencyConversionStub {

  def givenCurrencyConversion(code: String = "EUR")(implicit server: WireMockServer): StubMapping =
    server.stubFor(
      get(urlMatching(s"/currency-conversion/rates/(.*)?cc=$code"))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(Json.toJson(List(ConversionRatePeriod(now, now, code, BigDecimal(1.1)))).toString)
        )
    )
}
