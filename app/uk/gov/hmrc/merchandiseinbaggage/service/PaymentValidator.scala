/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import cats.Id
import cats.data.EitherT
import uk.gov.hmrc.merchandiseinbaggage.model.core._

trait PaymentValidator {

  def validateNewStatus(declaration: Declaration, newStatus: PaymentStatus): EitherT[Id, InvalidPaymentStatus.type, Declaration] =
    EitherT.fromOption(validPaymentStatusUpdates.get((declaration.paymentStatus, newStatus)), InvalidPaymentStatus)
      .map(status => declaration.copy(paymentStatus = status))

  def validateAmount(declaration: Declaration): EitherT[Id, InvalidAmount.type, Declaration] =
    EitherT.cond(declaration.amount.value >= 0.01, declaration, InvalidAmount)

  def validateTraderName(declaration: Declaration): EitherT[Id, InvalidName.type, Declaration] =
    EitherT.cond(!declaration.name.value.isEmpty, declaration, InvalidName)

  def validateChargeReference(declaration: Declaration): EitherT[Id, InvalidChargeReference.type, Declaration] =
    EitherT.cond(!declaration.reference.value.isEmpty, declaration, InvalidChargeReference)

  private val validPaymentStatusUpdates: Map[(PaymentStatus, PaymentStatus), PaymentStatus] = Map(
    (Outstanding, Paid)       -> Paid,
    (Outstanding, Reconciled) -> Reconciled,
    (Outstanding, Failed)     -> Failed
  )
}
