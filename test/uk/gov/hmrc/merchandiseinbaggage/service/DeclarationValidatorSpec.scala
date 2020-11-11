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

package uk.gov.hmrc.merchandiseinbaggage.service

import uk.gov.hmrc.merchandiseinbaggage.model.core.{ForeignAmount, ChargeReference, DeclarationBE, Failed, InvalidAmount, InvalidChargeReference, InvalidName, InvalidPaymentStatus, Outstanding, Paid, Reconciled, TraderName}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class DeclarationValidatorSpec extends BaseSpec with CoreTestData {

  "runs all validations" in new DeclarationValidator {
    val outstandingDeclaration = aDeclarationBE.copy(paymentStatus = Outstanding)

    validateRequest(outstandingDeclaration, Outstanding).value mustBe Left(InvalidPaymentStatus)
    validateAmount(aDeclarationBE.copy(amount = ForeignAmount(0))).value mustBe Left(InvalidAmount)
    validateTraderName(aDeclarationBE.copy(name = TraderName(""))).value mustBe Left(InvalidName)
    validateChargeReference(aDeclarationBE.copy(reference = ChargeReference(""))).value mustBe Left(InvalidChargeReference)
  }

  "payment status can only be updated to PAID & RECONCILED from OUTSTANDING" in new DeclarationValidator {
    val outstandingDeclaration = aDeclarationBE.copy(paymentStatus = Outstanding)

    validateNewStatus(outstandingDeclaration, Paid).value mustBe Right(outstandingDeclaration.withPaidStatus())
    validateNewStatus(outstandingDeclaration, Reconciled).value mustBe Right(outstandingDeclaration.withReconciledStatus())
    validateNewStatus(outstandingDeclaration, Failed).value mustBe Right(outstandingDeclaration.withFailedStatus())
  }

  "return InvalidPaymentStatus if trying update in to an invalid state" in new DeclarationValidator {
    val outstandingDeclaration = aDeclarationBE.copy(paymentStatus = Outstanding)

    validateNewStatus(outstandingDeclaration, Outstanding).value mustBe Left(InvalidPaymentStatus)
  }

  "validate amount by checking if at least 1 pence" in new DeclarationValidator {
    val declaration: DeclarationBE = aDeclarationBE.copy(amount = ForeignAmount(1))

    validateAmount(declaration).value mustBe Right(declaration)
    validateAmount(aDeclarationBE.copy(amount = ForeignAmount(0))).value mustBe Left(InvalidAmount)
  }

  "validate name by checking if empty" in new DeclarationValidator {
    val declaration: DeclarationBE = aDeclarationBE.copy(name = TraderName("Mr"))

    validateTraderName(declaration).value mustBe Right(declaration)
    validateTraderName(aDeclarationBE.copy(name = TraderName(""))).value mustBe Left(InvalidName)
  }

  "validate charge reference by checking if empty" in new DeclarationValidator {
    val declaration: DeclarationBE = aDeclarationBE.copy(reference = ChargeReference("456"))

    validateChargeReference(declaration).value mustBe Right(declaration)
    validateChargeReference(aDeclarationBE.copy(reference = ChargeReference(""))).value mustBe Left(InvalidChargeReference)
  }
}
