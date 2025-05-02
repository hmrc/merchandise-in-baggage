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

package uk.gov.hmrc.merchandiseinbaggage.model.api.calculation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.ThresholdCheck.format

class ThresholdCheckSpec extends AnyWordSpec with Matchers {

  "ThresholdCheck" should {

    "serialize to JSON" when {
      "the value is OverThreshold" in {
        Json.toJson(OverThreshold: ThresholdCheck) shouldBe JsString("OverThreshold")
      }

      "the value is WithinThreshold" in {
        Json.toJson(WithinThreshold: ThresholdCheck) shouldBe JsString("WithinThreshold")
      }
    }

    "deserialize from JSON" when {
      "the value is OverThreshold" in {
        val json = JsString("OverThreshold")
        json.validate[ThresholdCheck] shouldBe JsSuccess(OverThreshold)
      }

      "the value is WithinThreshold" in {
        val json = JsString("WithinThreshold")
        json.validate[ThresholdCheck] shouldBe JsSuccess(WithinThreshold)
      }
    }

    "fail deserialization" when {
      "the value is not a recognized string" in {
        val json = JsString("InvalidThreshold")
        json.validate[ThresholdCheck] shouldBe a[JsError]
      }

      "the value is not a string" in {
        val json = Json.obj("key" -> "value")
        json.validate[ThresholdCheck] shouldBe a[JsError]
      }
    }

    "support round-trip serialization and deserialization" in {
      val original: ThresholdCheck = OverThreshold
      val json                     = Json.toJson(original)
      json.validate[ThresholdCheck] shouldBe JsSuccess(original)
    }
  }
}
