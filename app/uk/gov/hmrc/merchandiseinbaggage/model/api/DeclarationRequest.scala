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

import java.time.LocalDateTime
import java.util.UUID.randomUUID

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.core._

case class DeclarationRequest(sessionId: SessionId,
                              declarationType: DeclarationType,
                              goodsDestination: GoodsDestination,
                              declarationGoods: DeclarationGoods,
                              nameOfPersonCarryingTheGoods: Name,
                              email: Email,
                              maybeCustomsAgent: Option[CustomsAgent],
                              eori: Eori,
                              journeyDetails: JourneyDetails,
                              dateOfDeclaration: LocalDateTime,
                              mibReference: MibReference,
                              maybeTotalCalculationResult: Option[TotalCalculationResult] = None
                             ) {
  lazy val obfuscated: DeclarationRequest =
    this.copy(
      nameOfPersonCarryingTheGoods = nameOfPersonCarryingTheGoods.obfuscated,
      email = email.obfuscated,
      maybeCustomsAgent = maybeCustomsAgent.map(_.obfuscated),
      eori = eori.obfuscated,
      journeyDetails = journeyDetails.obfuscated
    )
}

object DeclarationRequest {
  implicit val format: Format[DeclarationRequest] = Json.format

  implicit class ToDeclaration(declarationRequest: DeclarationRequest) {
    def toDeclaration: Declaration = {
      import declarationRequest._
      Declaration(DeclarationId(randomUUID().toString), sessionId, declarationType, goodsDestination, declarationGoods,
        nameOfPersonCarryingTheGoods, email, maybeCustomsAgent, eori, journeyDetails, dateOfDeclaration, mibReference, maybeTotalCalculationResult)
    }
  }

}
