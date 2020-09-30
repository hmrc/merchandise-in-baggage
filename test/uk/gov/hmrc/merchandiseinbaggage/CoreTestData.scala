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

import java.util.UUID

import uk.gov.hmrc.merchandiseinbaggage.model.api.{CalculationRequest, DeclarationRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.core._

trait CoreTestData {

  val aTraderName: TraderName = TraderName("name")
  val anAmountInPence: ForeignAmount = ForeignAmount(1)
  val aCsgTpsProviderId: CsgTpsProviderId = CsgTpsProviderId("123")
  val aChargeReference: ChargeReference = ChargeReference("ref")

  def aDeclaration: Declaration =
    Declaration(DeclarationId(UUID.randomUUID().toString),
      aTraderName, anAmountInPence, aCsgTpsProviderId, aChargeReference, Outstanding, None, None)

  def aPaymentRequest: DeclarationRequest = DeclarationRequest(aTraderName, anAmountInPence, aCsgTpsProviderId, aChargeReference)

  def aCalculationRequest: CalculationRequest = CalculationRequest("USD", anAmountInPence)

  implicit class WithPaidStatus(declaration: Declaration) {
    def withPaidStatus(): Declaration = declaration.copy(paymentStatus = Paid)
    def withReconciledStatus(): Declaration = declaration.copy(paymentStatus = Reconciled)
    def withFailedStatus(): Declaration = declaration.copy(paymentStatus = Failed)
  }
}
