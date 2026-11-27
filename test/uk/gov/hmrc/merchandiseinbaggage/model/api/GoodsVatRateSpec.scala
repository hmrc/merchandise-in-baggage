/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.model.api
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsNumber, JsString, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsVatRate.*

class GoodsVatRateSpec extends AnyWordSpec with Matchers {

  "GoodsVatRate Enum" should {

    "contain Zero, Five and Twenty values" in {
      GoodsVatRate.values should contain theSameElementsAs Seq(Zero, Five, Twenty)
    }

    "have the correct rate values" in {
      Zero.value   shouldBe 0
      Five.value   shouldBe 5
      Twenty.value shouldBe 20
    }

    "serialize to JSON" when {
      "value is Zero" in {
        Json.toJson[GoodsVatRate](Zero) shouldBe JsString("Zero")
      }

      "value is Five" in {
        Json.toJson[GoodsVatRate](Five) shouldBe JsString("Five")
      }

      "value is Twenty" in {
        Json.toJson[GoodsVatRate](Twenty) shouldBe JsString("Twenty")
      }
    }

    "deserialize from JSON" when {
      "value is 'Zero'" in {
        JsString("Zero").validate[GoodsVatRate] shouldBe JsSuccess(Zero)
      }

      "value is 'Five'" in {
        JsString("Five").validate[GoodsVatRate] shouldBe JsSuccess(Five)
      }

      "value is 'Twenty'" in {
        JsString("Twenty").validate[GoodsVatRate] shouldBe JsSuccess(Twenty)
      }

      "value is invalid" in {
        JsString("Ten").validate[GoodsVatRate] shouldBe JsError("Invalid Goods Vat Rate")
      }

      "value is not a string" in {
        JsNumber(5).validate[GoodsVatRate] shouldBe a[JsError]
      }
    }

    "round-trip serialization/deserialization" in {
      GoodsVatRate.values.foreach { value =>
        Json.toJson(value).validate[GoodsVatRate] shouldBe JsSuccess(value)
      }
    }
  }
}
