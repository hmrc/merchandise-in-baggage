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

package uk.gov.hmrc.merchandiseinbaggage.model.api

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsSuccess, Json}
import java.time.LocalDate

class JourneyOnFootSpec extends AnyWordSpec with Matchers {
  val validPort          = Port("LHR", "London Heathrow", isGB = true, portSynonyms = List("Heathrow", "LHR"))
  val validJourneyOnFoot = JourneyOnFoot(
    port = validPort,
    dateOfTravel = LocalDate.parse("2024-12-10")
  )

  "JourneyOnFoot" should {

    "serialize to JSON" when {
      "all fields are valid" in {
        Json.toJson(validJourneyOnFoot) shouldBe Json.obj(
          "port"         -> Json.obj(
            "code"         -> "LHR",
            "displayName"  -> "London Heathrow",
            "isGB"         -> true,
            "portSynonyms" -> Json.arr("Heathrow", "LHR")
          ),
          "dateOfTravel" -> "2024-12-10"
        )
      }
    }

    "deserialize from JSON" when {
      "all fields are valid" in {
        val json = Json.obj(
          "port"         -> Json.obj(
            "code"         -> "LHR",
            "displayName"  -> "London Heathrow",
            "isGB"         -> true,
            "portSynonyms" -> Json.arr("Heathrow", "LHR")
          ),
          "dateOfTravel" -> "2024-12-10"
        )

        json.validate[JourneyOnFoot] shouldBe JsSuccess(validJourneyOnFoot)
      }
    }

    "fail deserialization" when {
      "required fields are missing" in {
        val json = Json.obj(
          "dateOfTravel" -> "2024-12-10"
          // Missing "port"
        )

        json.validate[JourneyOnFoot] shouldBe a[JsError]
      }

      "dateOfTravel is missing" in {
        val json = Json.obj(
          "port" -> Json.obj(
            "code"         -> "LHR",
            "displayName"  -> "London Heathrow",
            "isGB"         -> true,
            "portSynonyms" -> Json.arr("Heathrow", "LHR")
          )
        )

        json.validate[JourneyOnFoot] shouldBe a[JsError]
      }

      "dateOfTravel is invalid" in {
        val json = Json.obj(
          "port"               -> Json.obj(
            "code"         -> "LHR",
            "displayName"  -> "London Heathrow",
            "isGB"         -> true,
            "portSynonyms" -> Json.arr("Heathrow", "LHR")
          ),
          "dateOfTravel"       -> "invalid-date",
          "registrationNumber" -> "AB123CD"
        )

        json.validate[JourneyOnFoot] shouldBe a[JsError]
      }
    }

    "handle edge cases" when {
      "dateOfTravel is an extreme date" in {
        val journey = validJourneyOnFoot.copy(dateOfTravel = LocalDate.of(1900, 1, 1))
        val json    = Json.toJson(journey)

        json.validate[JourneyOnFoot] shouldBe JsSuccess(journey)
      }

      "port contains special characters" in {
        val journey = validJourneyOnFoot.copy(port = validPort.copy(code = "LHR@123"))
        val json    = Json.toJson(journey)

        json.validate[JourneyOnFoot] shouldBe JsSuccess(journey)
      }
    }

    "support round-trip serialization/deserialization" in {
      val json = Json.toJson(validJourneyOnFoot)
      json.validate[JourneyOnFoot] shouldBe JsSuccess(validJourneyOnFoot)
    }
  }
}
