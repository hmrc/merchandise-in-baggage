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

package uk.gov.hmrc.merchandiseinbaggage.model.core

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsSuccess, Json, JsError}
import uk.gov.hmrc.merchandiseinbaggage.model.core.PaymentCallbackRequest.*

class PaymentCallbackRequestSpec extends AnyWordSpecLike with Matchers {

  "PaymentCallbackRequestSpec" should {
    "serialize to JSON" when {
      "with all fields are defined" in {
        val paymentCallbackRequest = PaymentCallbackRequest("mockChargeReference", Some(1))

        Json.toJson(paymentCallbackRequest) shouldBe Json.obj(
          "chargeReference" -> "mockChargeReference",
          "amendmentReference" -> 1
        )
      }
      "with amend reference is not provided" in {
        val paymentCallbackRequest = PaymentCallbackRequest("mockChargeReference", None)

        Json.toJson(paymentCallbackRequest) shouldBe Json.obj(
          "chargeReference" -> "mockChargeReference"
        )
      }
      "with default values" in {
        val paymentCallbackRequest = PaymentCallbackRequest("mockChargeReference")

        Json.toJson(paymentCallbackRequest) shouldBe Json.obj(
          "chargeReference" -> "mockChargeReference"
        )
      }
      "with round-trip serialization/deserialization" in {
        val paymentCallbackRequest = PaymentCallbackRequest("mockChargeReference", Some(1))
        val json = Json.toJson(paymentCallbackRequest)

        json.validate[PaymentCallbackRequest] shouldBe JsSuccess(paymentCallbackRequest)
      }
    }
    "deserialize to JSON" when {
      "with all fields are defined" in {
        val paymentCallbackRequest = PaymentCallbackRequest("mockChargeReference", Some(1))

        Json
          .obj(
            "chargeReference" -> "mockChargeReference",
            "amendmentReference" -> 1
          )
          .validate[PaymentCallbackRequest] shouldBe JsSuccess(paymentCallbackRequest)
      }
      "with amend reference is not provided" in {
        val paymentCallbackRequest = PaymentCallbackRequest("mockChargeReference", None)

        Json
          .obj(
            "chargeReference" -> "mockChargeReference"
          )
          .validate[PaymentCallbackRequest] shouldBe JsSuccess(paymentCallbackRequest)
      }
      "with empty string for required field" in {
        val paymentCallbackRequest = PaymentCallbackRequest("", Some(1))
        
        val json = Json.obj(
          "chargeReference" -> "",
          "amendmentReference" -> 1
        )

        json.validate[PaymentCallbackRequest] shouldBe JsSuccess(paymentCallbackRequest)
      }
      "with special characters in required field" in {
        val json = Json.obj(
          "chargeReference" -> "mock@Charge#123",
          "amendmentReference" -> 1
        )

        json.validate[PaymentCallbackRequest] shouldBe JsSuccess(
          PaymentCallbackRequest("mock@Charge#123", Some(1))
        )
      }
      "with extra fields" in {
        val json = Json.obj(
          "chargeReference" -> "mockChargeReference",
          "amendmentReference" -> 1,
          "extraField" -> "unexpected"
        )

        json.validate[PaymentCallbackRequest] shouldBe JsSuccess(
          PaymentCallbackRequest("mockChargeReference", Some(1))
        )
      }
      "with missing required field" in {
        val json = Json.obj(
          "amendmentReference" -> 1
        )

        json.validate[PaymentCallbackRequest] shouldBe a[JsError]
      }
      "with invalid type for optional field" in {
        val json = Json.obj(
          "chargeReference" -> "mockChargeReference",
          "amendmentReference" -> Json.arr(1, 2, 3)
        )

        json.validate[PaymentCallbackRequest] shouldBe a[JsError]
      }
      "with empty field types" in {
        val json = Json.obj()

        json.validate[PaymentCallbackRequest] shouldBe a[JsError]
      }
      "with invalid field types" in {
        val json = Json.obj(
          "chargeReference" -> 12345,
          "amendmentReference" -> Some(true)
        )
        json.validate[PaymentCallbackRequest] shouldBe a[JsError]
      }
      "with null optional field" in {
        val json = Json.obj(
          "chargeReference" -> "mockChargeReference",
          "amendmentReference" -> null
        )

        json.validate[PaymentCallbackRequest] shouldBe JsSuccess(
          PaymentCallbackRequest("mockChargeReference", None)
        )
      }
      "with invalid JSON structure (primitive value)" in {
        val json = Json.toJson("invalidStructure")

        json.validate[PaymentCallbackRequest] shouldBe a[JsError]
      }
    }

    
  }
}
