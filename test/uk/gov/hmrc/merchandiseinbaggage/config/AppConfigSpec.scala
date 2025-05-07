/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.merchandiseinbaggage.BaseSpecWithApplication

class AppConfigSpec extends BaseSpecWithApplication {

  override def fakeApplication(): Application = new GuiceApplicationBuilder().build()

  "AppConfig" should {
    "return the correct values" in {
      appConfig.bfEmail mustBe "foo@bar.com"
      appConfig.emailUrl mustBe "http://localhost:8300"
      appConfig.eoriCheckUrl mustBe "http://localhost:8351"
      appConfig.currencyConversionUrl mustBe "http://localhost:9016"
    }

    "throw an exception when the config value is not found" in {
      val exception = intercept[RuntimeException] {
        appConfig.config.get[String]("non-existent-key")
      }

      exception.getMessage must include("No configuration setting found for key 'non-existent-key'")
    }
  }
}
