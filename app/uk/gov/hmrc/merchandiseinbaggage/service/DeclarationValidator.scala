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

import cats.Id
import cats.data.EitherT
import uk.gov.hmrc.merchandiseinbaggage.model.core._

trait DeclarationValidator {

  def validateRequest(declaration: Declaration, newStatus: PaymentStatus): EitherT[Id, BusinessError, Declaration] =
    for {
      status    <- validateNewStatus(declaration, newStatus)
      amount    <- validateAmount(status)
      name      <- validateTraderName(amount)
      reference <- validateChargeReference(name)
    } yield reference

  protected def validateNewStatus(declaration: Declaration, newStatus: PaymentStatus): EitherT[Id, InvalidPaymentStatus.type, Declaration] =
    EitherT.fromOption(validPaymentStatusUpdates.get((declaration.paymentStatus, newStatus)), InvalidPaymentStatus)
      .map(status => declaration.copy(paymentStatus = status))

  protected def validateAmount(declaration: Declaration): EitherT[Id, BusinessError, Declaration] =
    EitherT.cond(declaration.amount.value >= 0.01, declaration, InvalidAmount)

  protected def validateTraderName(declaration: Declaration): EitherT[Id, BusinessError, Declaration] =
    EitherT.cond(!declaration.name.value.isEmpty, declaration, InvalidName)

  protected def validateChargeReference(declaration: Declaration): EitherT[Id, BusinessError, Declaration] =
    EitherT.cond(!declaration.reference.value.isEmpty, declaration, InvalidChargeReference)

  private val validPaymentStatusUpdates: Map[(PaymentStatus, PaymentStatus), PaymentStatus] = Map(
    (Outstanding, Paid)       -> Paid,
    (Outstanding, Reconciled) -> Reconciled,
    (Outstanding, Failed)     -> Failed
  )
}
