/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import uk.gov.hmrc.merchandiseinbaggage.model.core.{Failed, InvalidPaymentStatus, Outstanding, Paid, Reconciled}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class PaymentStatusValidatorSpec extends BaseSpec with CoreTestData {

  "payment status can only be updated to PAID & RECONCILED from OUTSTANDING" in new PaymentStatusValidator {
    val outstandingDeclaration = aDeclaration.copy(paymentStatus = Outstanding)

    validateNewStatus(outstandingDeclaration, Paid).value mustBe Right(outstandingDeclaration.withPaidStatus())
    validateNewStatus(outstandingDeclaration, Reconciled).value mustBe Right(outstandingDeclaration.withReconciledStatus())
    validateNewStatus(outstandingDeclaration, Failed).value mustBe Right(outstandingDeclaration.withFailedStatus())
  }

  "return InvalidPaymentStatus if trying update in to an invalid state" in new PaymentStatusValidator {
    val outstandingDeclaration = aDeclaration.copy(paymentStatus = Outstanding)

    validateNewStatus(outstandingDeclaration, Outstanding).value mustBe Left(InvalidPaymentStatus)
  }
}
