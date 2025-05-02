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

package uk.gov.hmrc.merchandiseinbaggage.model.audit

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsNumber, JsString, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.api.MibReference

class RefundableDeclarationSpec extends AnyWordSpec with Matchers {
  val validMibReference = MibReference("REF123")

  "RefundableDeclaration" should {
    val validDeclaration = RefundableDeclaration(
      mibReference = validMibReference,
      name = "John Doe",
      eori = "GB123456789000",
      goodsCategory = "Electronics",
      gbpValue = "1000",
      customsDuty = "100",
      vat = "200",
      vatRate = "20%",
      paymentAmount = "1300",
      producedInEu = "Yes",
      purchaseAmount = "1200",
      currencyCode = "USD",
      exchangeRate = "1.25"
    )

    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(validDeclaration) shouldBe Json.obj(
          "mibReference"   -> "REF123",
          "name"           -> "John Doe",
          "eori"           -> "GB123456789000",
          "goodsCategory"  -> "Electronics",
          "gbpValue"       -> "1000",
          "customsDuty"    -> "100",
          "vat"            -> "200",
          "vatRate"        -> "20%",
          "paymentAmount"  -> "1300",
          "producedInEu"   -> "Yes",
          "purchaseAmount" -> "1200",
          "currencyCode"   -> "USD",
          "exchangeRate"   -> "1.25"
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are valid" in {
        val json = Json.obj(
          "mibReference"   -> "REF123",
          "name"           -> "John Doe",
          "eori"           -> "GB123456789000",
          "goodsCategory"  -> "Electronics",
          "gbpValue"       -> "1000",
          "customsDuty"    -> "100",
          "vat"            -> "200",
          "vatRate"        -> "20%",
          "paymentAmount"  -> "1300",
          "producedInEu"   -> "Yes",
          "purchaseAmount" -> "1200",
          "currencyCode"   -> "USD",
          "exchangeRate"   -> "1.25"
        )

        json.validate[RefundableDeclaration] shouldBe JsSuccess(validDeclaration)
      }
    }

    "fail deserialization" when {
      "required fields are missing" in {
        val json = Json.obj(
          "name"          -> "John Doe",
          "eori"          -> "GB123456789000",
          "goodsCategory" -> "Electronics"
          // Missing other fields
        )

        json.validate[RefundableDeclaration] shouldBe a[JsError]
      }

      "field types are invalid" in {
        val json = Json.obj(
          "mibReference"   -> 123, // Should be a string
          "name"           -> "John Doe",
          "eori"           -> "GB123456789000",
          "goodsCategory"  -> "Electronics",
          "gbpValue"       -> true, // Should be a string
          "customsDuty"    -> "100",
          "vat"            -> "200",
          "vatRate"        -> "20%",
          "paymentAmount"  -> "1300",
          "producedInEu"   -> "Yes",
          "purchaseAmount" -> "1200",
          "currencyCode"   -> "USD",
          "exchangeRate"   -> "1.25"
        )

        json.validate[RefundableDeclaration] shouldBe a[JsError]
      }
    }

    "handle edge cases" when {
      "fields contain special characters" in {
        val declaration = validDeclaration.copy(
          name = "John@Doe#123",
          goodsCategory = "Electronics!$%",
          currencyCode = "USD$"
        )

        val json = Json.toJson(declaration)
        json.validate[RefundableDeclaration] shouldBe JsSuccess(declaration)
      }

      "fields contain large numerical strings" in {
        val declaration = validDeclaration.copy(
          gbpValue = "1000000000000",
          customsDuty = "999999999",
          vat = "999999999999"
        )

        val json = Json.toJson(declaration)
        json.validate[RefundableDeclaration] shouldBe JsSuccess(declaration)
      }
    }

    "support round-trip serialization/deserialization" in {
      val json = Json.toJson(validDeclaration)
      json.validate[RefundableDeclaration] shouldBe JsSuccess(validDeclaration)
    }
  }

  "MibReference" should {
    "serialize to JSON" in {
      Json.toJson(validMibReference) shouldBe JsString("REF123")
    }

    "deserialize from JSON" in {
      val json = JsString("REF123")

      json.validate[MibReference] shouldBe JsSuccess(validMibReference)
    }

    "fail deserialization when invalid" in {
      val json = JsNumber(123)

      json.validate[MibReference] shouldBe a[JsError]
    }
  }
}
