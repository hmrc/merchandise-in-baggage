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
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNoDontKnow.*

class YesNoDontKnowSpec extends AnyWordSpec with Matchers {

  "YesNoDontKnow Enum" should {

    "contain Yes, No and DontKnow values" in {
      YesNoDontKnow.values should contain theSameElementsAs Seq(Yes, No, DontKnow)
    }

    "have the correct message keys" in {
      Yes.messageKey      shouldBe "yndk.Yes"
      No.messageKey       shouldBe "yndk.No"
      DontKnow.messageKey shouldBe "yndk.DontKnow"
    }

    "serialize to JSON" when {
      "value is Yes" in {
        Json.toJson[YesNoDontKnow](Yes) shouldBe JsString("Yes")
      }

      "value is No" in {
        Json.toJson[YesNoDontKnow](No) shouldBe JsString("No")
      }

      "value is DontKnow" in {
        Json.toJson[YesNoDontKnow](DontKnow) shouldBe JsString("DontKnow")
      }
    }

    "deserialize from JSON" when {
      "value is 'Yes'" in {
        JsString("Yes").validate[YesNoDontKnow] shouldBe JsSuccess(Yes)
      }

      "value is 'No'" in {
        JsString("No").validate[YesNoDontKnow] shouldBe JsSuccess(No)
      }

      "value is 'DontKnow'" in {
        JsString("DontKnow").validate[YesNoDontKnow] shouldBe JsSuccess(DontKnow)
      }

      "value is invalid" in {
        JsString("Maybe").validate[YesNoDontKnow] shouldBe JsError("Invalid Yes No Dont Know")
      }

      "value is not a string" in {
        JsNumber(1).validate[YesNoDontKnow] shouldBe a[JsError]
      }
    }

    "round-trip serialization/deserialization" in {
      YesNoDontKnow.values.foreach { value =>
        Json.toJson(value).validate[YesNoDontKnow] shouldBe JsSuccess(value)
      }
    }
  }
}
