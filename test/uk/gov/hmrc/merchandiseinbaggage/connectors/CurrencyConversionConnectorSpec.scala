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

import uk.gov.hmrc.merchandiseinbaggage.model.api.ConversionRatePeriod
import uk.gov.hmrc.merchandiseinbaggage.stubs.CurrencyConversionStub
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, WireMock}

import java.time.LocalDate

class CurrencyConversionConnectorSpec extends BaseSpecWithApplication with WireMock {
  private val connector = app.injector.instanceOf[CurrencyConversionConnector]

  "handles email requests" in {
    CurrencyConversionStub.givenCurrencyConversion("USD")
    val date = LocalDate.now()
    connector.getConversionRate("USD").futureValue mustBe Seq(ConversionRatePeriod(date, date, "USD", 1.1))
  }
}
