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

package uk.gov.hmrc.merchandiseinbaggage

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID.randomUUID

import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.Ports.Dover
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.core._

trait CoreTestData {

  def aDeclarationId: DeclarationId = DeclarationId(randomUUID().toString)

  private val aSessionId = SessionId("123456789")
  private val aGoodDestination = GreatBritain
  private val aDeclarationGoods = DeclarationGoods(Seq[Goods]())
  private val aName = Name("Terry", "Crews")
  private val anEori = Eori("eori-test")
  private val anEmail = Email("someone@", "someone@")
  private val aJourneyDetails = JourneyOnFootViaVehiclePort(Dover, LocalDate.now())
  private val aMibReference = MibReference("mib-ref-1234")

  def aDeclaration: Declaration = Declaration(aDeclarationId, aSessionId, Import, aGoodDestination, aDeclarationGoods,
    aName, anEmail, None, anEori, aJourneyDetails, LocalDateTime.now, aMibReference)

  def aDeclarationRequest: DeclarationRequest = DeclarationRequest(aSessionId, Import, aGoodDestination, aDeclarationGoods,
    aName, anEmail, None, anEori, aJourneyDetails, LocalDateTime.now, aMibReference)

  val aCustomsAgent: CustomsAgent =
    CustomsAgent(
      "Andy Agent", Address(Seq("1 Agent Drive", "Agent Town"), Some("AG1 5NT"), Country("GB", Some("United Kingdom"))))

  val aJourneyInASmallVehicle: JourneyInSmallVehicle = JourneyInSmallVehicle(Dover, LocalDate.now(), "licence")
}
