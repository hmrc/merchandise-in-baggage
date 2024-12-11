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
import play.api.libs.json.{Json, JsSuccess, JsError}

class DeclarationEmailInfoSpec extends AnyWordSpec with Matchers {
  "DeclarationEmailInfo" should {

    val validEmailInfo = DeclarationEmailInfo(
      to = Seq("example@test.com", "user@example.com"),
      templateId = "template123",
      parameters = Map("param1" -> "value1", "param2" -> "value2"),
      force = true,
      eventUrl = Some("https://event.url"),
      onSendUrl = Some("https://onsend.url")
    )

    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(validEmailInfo) shouldBe Json.obj(
          "to" -> Json.arr("example@test.com", "user@example.com"),
          "templateId" -> "template123",
          "parameters" -> Json.obj("param1" -> "value1", "param2" -> "value2"),
          "force" -> true,
          "eventUrl" -> "https://event.url",
          "onSendUrl" -> "https://onsend.url"
        )
      }

      "optional fields are None" in {
        val emailInfo = validEmailInfo.copy(eventUrl = None, onSendUrl = None)

        Json.toJson(emailInfo) shouldBe Json.obj(
          "to" -> Json.arr("example@test.com", "user@example.com"),
          "templateId" -> "template123",
          "parameters" -> Json.obj("param1" -> "value1", "param2" -> "value2"),
          "force" -> true
        )
      }

      "force is default (false)" in {
        val emailInfo = validEmailInfo.copy(force = false)

        Json.toJson(emailInfo) shouldBe Json.obj(
          "to" -> Json.arr("example@test.com", "user@example.com"),
          "templateId" -> "template123",
          "parameters" -> Json.obj("param1" -> "value1", "param2" -> "value2"),
          "force" -> false,
          "eventUrl" -> "https://event.url",
          "onSendUrl" -> "https://onsend.url"
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are valid" in {
        val json = Json.obj(
          "to" -> Json.arr("example@test.com", "user@example.com"),
          "templateId" -> "template123",
          "parameters" -> Json.obj("param1" -> "value1", "param2" -> "value2"),
          "force" -> true,
          "eventUrl" -> "https://event.url",
          "onSendUrl" -> "https://onsend.url"
        )

        json.validate[DeclarationEmailInfo] shouldBe JsSuccess(validEmailInfo)
      }

      "optional fields are missing" in {
        val json = Json.obj(
          "to" -> Json.arr("example@test.com", "user@example.com"),
          "templateId" -> "template123",
          "parameters" -> Json.obj("param1" -> "value1", "param2" -> "value2"),
          "force" -> true
        )

        json.validate[DeclarationEmailInfo] shouldBe JsSuccess(
          validEmailInfo.copy(eventUrl = None, onSendUrl = None)
        )
      }

      "force is missing and defaults to false" in {
        val json = Json.obj(
          "to" -> Json.arr("example@test.com", "user@example.com"),
          "templateId" -> "template123",
          "parameters" -> Json.obj("param1" -> "value1", "param2" -> "value2"),
          "eventUrl" -> "https://event.url",
          "onSendUrl" -> "https://onsend.url"
        )

        json.validate[DeclarationEmailInfo] shouldBe JsSuccess(validEmailInfo.copy(force = false))
      }
    }

    "fail deserialization" when {
      "required fields are missing" in {
        val json = Json.obj(
          "templateId" -> "template123",
          "parameters" -> Json.obj("param1" -> "value1", "param2" -> "value2")
        )

        json.validate[DeclarationEmailInfo] shouldBe a[JsError]
      }

      "field types are invalid" in {
        val json = Json.obj(
          "to" -> "invalidType", // Should be an array
          "templateId" -> "template123",
          "parameters" -> Json.obj("param1" -> "value1", "param2" -> "value2"),
          "force" -> "true", // Should be boolean
          "eventUrl" -> "https://event.url",
          "onSendUrl" -> "https://onsend.url"
        )

        json.validate[DeclarationEmailInfo] shouldBe a[JsError]
      }
    }

    "handle edge cases" when {
      "to is empty" in {
        val json = Json.obj(
          "to" -> Json.arr(),
          "templateId" -> "template123",
          "parameters" -> Json.obj("param1" -> "value1"),
          "force" -> false
        )

        json.validate[DeclarationEmailInfo] shouldBe JsSuccess(
          DeclarationEmailInfo(Seq.empty, "template123", Map("param1" -> "value1"), force = false)
        )
      }

      "parameters are empty" in {
        val json = Json.obj(
          "to" -> Json.arr("example@test.com"),
          "templateId" -> "template123",
          "parameters" -> Json.obj(),
          "force" -> false
        )

        json.validate[DeclarationEmailInfo] shouldBe JsSuccess(
          DeclarationEmailInfo(Seq("example@test.com"), "template123", Map.empty, force = false)
        )
      }

      "parameters contain special characters" in {
        val json = Json.obj(
          "to" -> Json.arr("example@test.com"),
          "templateId" -> "template123",
          "parameters" -> Json.obj("key" -> "!@#$%^&*()_+=|<>"),
          "force" -> false
        )

        json.validate[DeclarationEmailInfo] shouldBe JsSuccess(
          DeclarationEmailInfo(
            Seq("example@test.com"),
            "template123",
            Map("key" -> "!@#$%^&*()_+=|<>"),
            force = false
          )
        )
      }
    }

    "support round-trip serialization/deserialization" in {
      val json = Json.toJson(validEmailInfo)
      json.validate[DeclarationEmailInfo] shouldBe JsSuccess(validEmailInfo)
    }
  }
}
