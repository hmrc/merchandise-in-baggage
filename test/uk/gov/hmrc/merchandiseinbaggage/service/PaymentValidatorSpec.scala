/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import uk.gov.hmrc.merchandiseinbaggage.model.core.{Amount, ChargeReference, Declaration, Failed, InvalidAmount, InvalidChargeReference, InvalidName, InvalidPaymentStatus, Outstanding, Paid, Reconciled, TraderName}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class PaymentValidatorSpec extends BaseSpec with CoreTestData {

  "payment status can only be updated to PAID & RECONCILED from OUTSTANDING" in new PaymentValidator {
    val outstandingDeclaration = aDeclaration.copy(paymentStatus = Outstanding)

    validateNewStatus(outstandingDeclaration, Paid).value mustBe Right(outstandingDeclaration.withPaidStatus())
    validateNewStatus(outstandingDeclaration, Reconciled).value mustBe Right(outstandingDeclaration.withReconciledStatus())
    validateNewStatus(outstandingDeclaration, Failed).value mustBe Right(outstandingDeclaration.withFailedStatus())
  }

  "return InvalidPaymentStatus if trying update in to an invalid state" in new PaymentValidator {
    val outstandingDeclaration = aDeclaration.copy(paymentStatus = Outstanding)

    validateNewStatus(outstandingDeclaration, Outstanding).value mustBe Left(InvalidPaymentStatus)
  }

  "validate amount by checking if at least 1 pence" in new PaymentValidator {
    val declaration: Declaration = aDeclaration.copy(amount = Amount(0.01))

    validateAmount(declaration).value mustBe Right(declaration)
    validateAmount(aDeclaration.copy(amount = Amount(0.0))).value mustBe Left(InvalidAmount)
  }

  "validate name by checking if empty" in new PaymentValidator {
    val declaration: Declaration = aDeclaration.copy(name = TraderName("Mr"))

    validateTraderName(declaration).value mustBe Right(declaration)
    validateTraderName(aDeclaration.copy(name = TraderName(""))).value mustBe Left(InvalidName)
  }

  "validate charge reference by checking if empty" in new PaymentValidator {
    val declaration: Declaration = aDeclaration.copy(reference = ChargeReference("456"))

    validateChargeReference(declaration).value mustBe Right(declaration)
    validateChargeReference(aDeclaration.copy(reference = ChargeReference(""))).value mustBe Left(InvalidChargeReference)
  }
}
