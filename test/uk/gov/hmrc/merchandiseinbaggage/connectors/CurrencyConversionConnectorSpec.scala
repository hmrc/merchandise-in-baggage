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

package uk.gov.hmrc.merchandiseinbaggage.connectors

import java.time.LocalDate

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.model.api.CurrencyConversionResponse
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CurrencyConversionStub}

class CurrencyConversionConnectorSpec extends BaseSpecWithApplication with CurrencyConversionStub with ScalaFutures {
  "retrieve currency conversion" in {
    val connector = injector.instanceOf[CurrencyConversionConnector]
    val currencyCode = "USD"
    val conversionResponse: CurrencyConversionResponse = CurrencyConversionResponse(currencyCode, Some("1.3064"))

    getCurrencyConversionStub(currencyCode)
    connector.findCurrencyRate(currencyCode, LocalDate.now).futureValue mustBe List(conversionResponse)
  }
}
