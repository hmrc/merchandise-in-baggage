/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage

import java.util.UUID

import uk.gov.hmrc.merchandiseinbaggage.model.api.PaymentRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core._

trait CoreTestData {

  val aTraderName = TraderName("name")
  val anAmount = Amount(1)
  val aCsgTpsProviderId = CsgTpsProviderId("123")
  val aChargeReference = ChargeReference("ref")

  def aDeclaration =
    Declaration(DeclarationId(UUID.randomUUID().toString),
      aTraderName, anAmount, aCsgTpsProviderId, aChargeReference, Outstanding, None, None)

  def aPaymentRequest = PaymentRequest(aTraderName, anAmount, aCsgTpsProviderId, aChargeReference)

  implicit class WithPaidStatus(declaration: Declaration) {
    def withPaidStatus(): Declaration = declaration.copy(paymentStatus = Paid)
    def withReconciledStatus(): Declaration = declaration.copy(paymentStatus = Reconciled)
    def withFailedStatus(): Declaration = declaration.copy(paymentStatus = Failed)
  }
}
