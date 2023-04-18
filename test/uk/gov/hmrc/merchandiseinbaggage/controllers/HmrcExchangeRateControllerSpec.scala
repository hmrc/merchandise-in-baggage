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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.libs.json.Json
import play.api.test.Helpers.{contentAsJson, status, _}
import uk.gov.hmrc.merchandiseinbaggage.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggage.model.api.ExchangeRateURL

import java.time.LocalDate

class HmrcExchangeRateControllerSpec extends BaseSpecWithApplication {

  val controller: HmrcExchangeRateController = injector.instanceOf[HmrcExchangeRateController]

  "return with an expected yearly URI" in {

    val result = controller.yearlyUrl()(fakeRequest)

    status(result) mustBe 200
    contentAsJson(result) mustBe Json.toJson(
      ExchangeRateURL(
        s"https://www.gov.uk/government/publications/hmrc-exchange-rates-for-${LocalDate.now().getYear}-monthly"
      )
    )
  }
}
