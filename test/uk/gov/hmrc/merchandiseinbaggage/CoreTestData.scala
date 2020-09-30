/*
 * Copyright 2020 HM Revenue & Customs
 *
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
