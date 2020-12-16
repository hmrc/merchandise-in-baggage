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

import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.core._

import java.time.{LocalDate, LocalDateTime}
import java.util.UUID.randomUUID

trait CoreTestData {

  def aDeclarationId: DeclarationId = DeclarationId(randomUUID().toString)

  private val aSessionId = SessionId("123456789")
  private val aGoodDestination = GreatBritain
  private val aDeclarationGoods = DeclarationGoods(Seq[Goods](Goods(CategoryQuantityOfGoods("test", "1"), GoodsVatRates.Five, Country("GB", "United Kingdom", "GB", isEu=true, List("England", "Scotland", "Wales", "Northern Ireland", "GB", "UK")), PurchaseDetails("100", Currency("GBP", "title.euro_eur", Some("GBP"), List("Europe", "European"))))))
  private val aName = Name("Terry", "Crews")
  private val anEori = Eori("eori-test")
  private val anEmail = Email("someone@", "someone@")
  private val aJourneyDetails = JourneyOnFoot(Port("DVR", "title.dover", isGB=true, List("Port of Dover")), LocalDate.now())
  private val aMibReference = MibReference("mib-ref-1234")
  private val paymentCalculations = PaymentCalculations(aDeclarationGoods.goods.map(good => PaymentCalculation(good, CalculationResult(AmountInPence(100), AmountInPence(100), AmountInPence(100)))))
  private val totalCalculationResult = TotalCalculationResult(paymentCalculations, AmountInPence(100), AmountInPence(100), AmountInPence(100), AmountInPence(100))

  def aDeclaration: Declaration = Declaration(aDeclarationId, aSessionId, Import, aGoodDestination, aDeclarationGoods,
    aName, anEmail, None, anEori, aJourneyDetails, LocalDateTime.now, aMibReference, Some(totalCalculationResult))

  val aCustomsAgent: CustomsAgent =
    CustomsAgent(
      "Andy Agent", Address(Seq("1 Agent Drive", "Agent Town"), Some("AG1 5NT"), AddressLookupCountry("GB", Some("UK"))))

  val aJourneyInASmallVehicle: JourneyInSmallVehicle = JourneyInSmallVehicle(Port("DVR", "title.dover", isGB=true, List("Port of Dover")), LocalDate.now(), "licence")
}
