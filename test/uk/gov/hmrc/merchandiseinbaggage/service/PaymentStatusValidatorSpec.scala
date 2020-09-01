package uk.gov.hmrc.merchandiseinbaggage.service

import uk.gov.hmrc.merchandiseinbaggage.model.core.{InvalidPaymentStatus, Outstanding, Paid, Reconciled}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class PaymentStatusValidatorSpec extends BaseSpec with CoreTestData {

  "payment status can only be updated to PAID & RECONCILED from OUTSTANDING" in new PaymentStatusValidator {
    val outstandingDeclaration = aDeclaration.copy(paymentStatus = Outstanding)

    validateNewStatus(outstandingDeclaration, Paid) mustBe Right(outstandingDeclaration.withPaidStatus())
    validateNewStatus(outstandingDeclaration, Reconciled) mustBe Right(outstandingDeclaration.withReconciledStatus())
  }

  "return InvalidPaymentStatus if trying update in to an invalid state" in new PaymentStatusValidator {
    val outstandingDeclaration = aDeclaration.copy(paymentStatus = Outstanding)

    validateNewStatus(outstandingDeclaration, Outstanding) mustBe Left(InvalidPaymentStatus)
  }
}
