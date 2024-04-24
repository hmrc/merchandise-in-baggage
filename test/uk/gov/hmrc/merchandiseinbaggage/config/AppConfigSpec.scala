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

package uk.gov.hmrc.merchandiseinbaggage.config

import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, WireMock}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig.{CurrencyConversionConf, EmailConf, EoriCheckConf}

class AppConfigSpec extends BaseSpecWithApplication with Matchers {

  "AppConfig" should {
    "return the correct values" in {
      appConfig.bfEmail mustBe "foo@bar.com"

      appConfig.eoriCheckConf mustBe EoriCheckConf(protocol = "http", port = WireMock.port)

      appConfig.currencyConversionConf mustBe CurrencyConversionConf(protocol = "http", port = WireMock.port)

      appConfig.emailConf mustBe EmailConf(protocol = "http")
    }

    "throw an exception when the config value is not found" in {
      val conf: AppConfig = app.injector.instanceOf[AppConfig]
      val exception = intercept[RuntimeException] {
        conf.config.get[String]("non-existent-key")
      }

      exception.getMessage must include("No configuration setting found for key 'non-existent-key'")
    }
  }

}
