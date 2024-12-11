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

package uk.gov.hmrc.merchandiseinbaggage.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.api.*

class ImportGoodsSpec extends AnyWordSpec with Matchers {
  val validPurchaseDetails =
    PurchaseDetails("100.00", Currency("USD", "United States Dollar", Some("USD"), List("Dollar", "US")))
  val validImportGoods     = ImportGoods(
    category = "Electronics",
    goodsVatRate = GoodsVatRates.Five,
    producedInEu = YesNoDontKnow.Yes,
    purchaseDetails = validPurchaseDetails
  )

  "ImportGoods" should {

    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(validImportGoods) shouldBe Json.obj(
          "category"        -> "Electronics",
          "goodsVatRate"    -> "Five",
          "producedInEu"    -> "Yes",
          "purchaseDetails" -> Json.obj(
            "amount"   -> "100.00",
            "currency" -> Json.obj(
              "code"               -> "USD",
              "displayName"        -> "United States Dollar",
              "valueForConversion" -> "USD",
              "currencySynonyms"   -> Json.arr("Dollar", "US")
            )
          )
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are valid" in {
        val json = Json.obj(
          "category"        -> "Electronics",
          "goodsVatRate"    -> "Five",
          "producedInEu"    -> "Yes",
          "purchaseDetails" -> Json.obj(
            "amount"   -> "100.00",
            "currency" -> Json.obj(
              "code"               -> "USD",
              "displayName"        -> "United States Dollar",
              "valueForConversion" -> "USD",
              "currencySynonyms"   -> Json.arr("Dollar", "US")
            )
          )
        )

        json.validate[ImportGoods] shouldBe JsSuccess(validImportGoods)
      }
    }

    "fail deserialization" when {
      "required fields are missing" in {
        val json = Json.obj(
          "category"     -> "Electronics",
          "goodsVatRate" -> "Standard"
          // Missing "producedInEu" and "purchaseDetails"
        )

        json.validate[ImportGoods] shouldBe a[JsError]
      }

      "field types are invalid" in {
        val json = Json.obj(
          "category"        -> "Electronics",
          "goodsVatRate"    -> 123, // Invalid type
          "producedInEu"    -> "Yes",
          "purchaseDetails" -> Json.obj(
            "amount"   -> "100.00",
            "currency" -> Json.obj(
              "code"               -> "USD",
              "displayName"        -> "United States Dollar",
              "valueForConversion" -> "USD",
              "currencySynonyms"   -> Json.arr("Dollar", "US")
            )
          )
        )

        json.validate[ImportGoods] shouldBe a[JsError]
      }
    }

    "handle edge cases" when {
      "category contains special characters" in {
        val goods = validImportGoods.copy(category = "Electronics!@#$%^&*")
        val json  = Json.toJson(goods)

        json.validate[ImportGoods] shouldBe JsSuccess(goods)
      }

      "producedInEu has different values" in {
        val goods = validImportGoods.copy(producedInEu = YesNoDontKnow.No)
        val json  = Json.toJson(goods)

        json.validate[ImportGoods] shouldBe JsSuccess(goods)
      }

      "purchaseDetails contains a very large amount" in {
        val largePurchaseDetails = validPurchaseDetails.copy(amount = "1000000000.00")
        val goods                = validImportGoods.copy(purchaseDetails = largePurchaseDetails)
        val json                 = Json.toJson(goods)

        json.validate[ImportGoods] shouldBe JsSuccess(goods)
      }
    }

    "support round-trip serialization/deserialization" in {
      val json = Json.toJson(validImportGoods)
      json.validate[ImportGoods] shouldBe JsSuccess(validImportGoods)
    }
  }

}
