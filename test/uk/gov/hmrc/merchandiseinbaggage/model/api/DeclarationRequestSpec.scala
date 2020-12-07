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
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class DeclarationRequestSpec extends BaseSpec with CoreTestData {

  "Serialise/Deserialise from/to json to PaymentRequest" in {
    val declarationRequest = aDeclarationRequest
    val actual = toJson(declarationRequest).toString

    toJson(declarationRequest) mustBe Json.parse(actual)
  }

  "convert a declaration request in to a declaration" in {
    val actualDeclaration: Declaration = aDeclarationRequest.toDeclaration

    actualDeclaration must matchPattern { case Declaration(_, _, _, _, _, _, _, _, _, _, _, _, _) => }
  }

  "be obfuscated" in {
    val declarationRequest =
      aDeclarationRequest.copy(maybeCustomsAgent = Some(aCustomsAgent), journeyDetails = aJourneyInASmallVehicle)

    declarationRequest.obfuscated.nameOfPersonCarryingTheGoods mustBe Name("*****", "*****")
    declarationRequest.obfuscated.email mustBe Email("********", "********")
    declarationRequest.obfuscated.maybeCustomsAgent.get.name mustBe "**********"
    declarationRequest.obfuscated.maybeCustomsAgent.get.address mustBe
      Address(Seq("*************", "**********"), Some("*******"), AddressLookupCountry("**", Some("**")))
    declarationRequest.obfuscated.eori mustBe Eori("*********")
    declarationRequest.obfuscated.journeyDetails.maybeRegistrationNumber mustBe Some("*******")
  }
}
