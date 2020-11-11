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

import java.time.LocalDate
import java.util.UUID

import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.Ports.Dover
import uk.gov.hmrc.merchandiseinbaggage.model.api.{CalculationRequest, Declaration, DeclarationGoods, DeclarationRequest, Eori, Goods, JourneyOnFootViaVehiclePort, MibReference, Name, SessionId}
import uk.gov.hmrc.merchandiseinbaggage.model.core._

trait CoreTestData {

  val aTraderName: TraderName = TraderName("name")
  val anAmountInPence: ForeignAmount = ForeignAmount(1)
  val aCsgTpsProviderId: CsgTpsProviderId = CsgTpsProviderId("123")
  val aChargeReference: ChargeReference = ChargeReference("ref")

  def aDeclarationBE: DeclarationBE =
    DeclarationBE(DeclarationId(UUID.randomUUID().toString),
      aTraderName, anAmountInPence, aCsgTpsProviderId, aChargeReference, Outstanding, None, None)

  def aPaymentRequest: DeclarationRequest = DeclarationRequest(aTraderName, anAmountInPence, aCsgTpsProviderId, aChargeReference)

  def aCalculationRequest: CalculationRequest = CalculationRequest("USD", anAmountInPence)

  implicit class WithPaidStatus(declaration: DeclarationBE) {
    def withPaidStatus(): DeclarationBE = declaration.copy(paymentStatus = Paid)
    def withReconciledStatus(): DeclarationBE = declaration.copy(paymentStatus = Reconciled)
    def withFailedStatus(): DeclarationBE = declaration.copy(paymentStatus = Failed)
  }

  val aSessionId = SessionId("123456789")
  val aGoodDestination = GreatBritain
  val aDeclarationGoods = DeclarationGoods(Seq[Goods]())
  val aName = Name("Terry", "Crews")
  val aEori = Eori("eori-test")
  val aJourneyDetails = JourneyOnFootViaVehiclePort(Dover, LocalDate.now())
  val aMibReference = MibReference("mib-ref-1234")
  val aDeclaration: Declaration = Declaration(aSessionId, Import, aGoodDestination, aDeclarationGoods,
    aName, None, aEori, aJourneyDetails, aMibReference)
}
