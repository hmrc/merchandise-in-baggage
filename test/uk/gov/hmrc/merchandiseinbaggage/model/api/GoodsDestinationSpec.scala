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
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestination.*

class GoodsDestinationSpec extends AnyWordSpec with Matchers {

  "GoodsDestination Enum" should {

    "contain NorthernIreland and GreatBritain values" in {
      GoodsDestination.values should contain theSameElementsAs Seq(NorthernIreland, GreatBritain)
    }

    "have the correct thresholds" when {
      "value is NorthernIreland" in {
        NorthernIreland.threshold shouldBe AmountInPence(87300)
      }

      "value is GreatBritain" in {
        GreatBritain.threshold shouldBe AmountInPence(250000)
      }
    }

    "serialize to JSON" when {
      "value is NorthernIreland" in {
        Json.toJson[GoodsDestination](NorthernIreland) shouldBe JsString("NorthernIreland")
      }

      "value is GreatBritain" in {
        Json.toJson[GoodsDestination](GreatBritain) shouldBe JsString("GreatBritain")
      }
    }

    "deserialize from JSON" when {
      "value is 'NorthernIreland'" in {
        JsString("NorthernIreland").validate[GoodsDestination] shouldBe JsSuccess(NorthernIreland)
      }

      "value is 'GreatBritain'" in {
        JsString("GreatBritain").validate[GoodsDestination] shouldBe JsSuccess(GreatBritain)
      }

      "value is invalid" in {
        JsString("France").validate[GoodsDestination] shouldBe JsError("Invalid Goods Destination")
      }

      "value is not a string" in {
        JsNumber(1).validate[GoodsDestination] shouldBe a[JsError]
      }
    }

    "round-trip serialization/deserialization" in {
      GoodsDestination.values.foreach { value =>
        Json.toJson(value).validate[GoodsDestination] shouldBe JsSuccess(value)
      }
    }
  }
}
