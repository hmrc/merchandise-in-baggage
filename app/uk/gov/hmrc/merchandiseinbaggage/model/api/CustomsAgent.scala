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

import play.api.libs.json.*
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.Address

case class CustomsAgent(name: String, address: Address)

object CustomsAgent {
  given format: OFormat[CustomsAgent] = new OFormat[CustomsAgent] {
    override def reads(json: JsValue): JsResult[CustomsAgent] =
      for {
        name    <- (json \ "name").validate[String].flatMap { n =>
                     if (n.nonEmpty) JsSuccess(n)
                     else JsError("name cannot be empty")
                   }
        address <- (json \ "address").validate[Address]
      } yield CustomsAgent(name, address)

    override def writes(agent: CustomsAgent): JsObject = Json.obj(
      "name"    -> agent.name,
      "address" -> agent.address
    )
  }
}
