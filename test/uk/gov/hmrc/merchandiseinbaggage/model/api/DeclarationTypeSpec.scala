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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.*

class DeclarationTypeSpec extends AnyWordSpec with Matchers {

  "DeclarationType Enum" should {

    "contain Import and Export values" in {
      DeclarationType.values should contain theSameElementsAs Seq(Import, Export)
    }

    "serialize to JSON" when {
      "value is Import" in {
        Json.toJson[DeclarationType](Import) shouldBe JsString("Import")
      }

      "value is Export" in {
        Json.toJson[DeclarationType](Export) shouldBe JsString("Export")
      }
    }

    "deserialize from JSON" when {
      "value is 'Import'" in {
        JsString("Import").validate[DeclarationType] shouldBe JsSuccess(Import)
      }

      "value is 'Export'" in {
        JsString("Export").validate[DeclarationType] shouldBe JsSuccess(Export)
      }

      "value is invalid" in {
        JsString("Transit").validate[DeclarationType] shouldBe JsError("Invalid Declaration Type")
      }

      "value is not a string" in {
        JsNumber(1).validate[DeclarationType] shouldBe a[JsError]
      }
    }

    "round-trip serialization/deserialization" in
      DeclarationType.values.foreach { value =>
        Json.toJson(value).validate[DeclarationType] shouldBe JsSuccess(value)
      }
  }
}
