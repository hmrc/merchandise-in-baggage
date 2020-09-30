/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Declaration, Outstanding}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class DeclarationRequestSpec extends BaseSpec with CoreTestData {

  "Serialise/Deserialise from/to json to PaymentRequest" in {
    val paymentRequest = aPaymentRequest
    val actual = Json.toJson(paymentRequest).toString

    Json.toJson(paymentRequest) mustBe Json.parse(actual)
  }

  "convert a payment request in to an outstanding declaration with no recorded payments and reconciliation" in {
    val actualDeclaration: Declaration = aPaymentRequest.toDeclarationInInitialState

    actualDeclaration must matchPattern { case Declaration(_, _, _, _, _, _, _, _) => }
    actualDeclaration.paymentStatus mustBe Outstanding
    actualDeclaration.paid mustBe empty
    actualDeclaration.reconciled mustBe empty
  }
}
