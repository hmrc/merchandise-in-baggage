/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import cats.Id
import cats.data.EitherT
import uk.gov.hmrc.merchandiseinbaggage.model.core._

trait DeclarationValidator {

  def validateRequest(declaration: Declaration, newStatus: PaymentStatus): EitherT[Id, BusinessError, Declaration] =
    for {
      status    <- validateNewStatus(declaration, newStatus)
      validated <- validatePersistRequest(status)
    } yield validated

  def validatePersistRequest(declaration: Declaration): EitherT[Id, BusinessError, Declaration] =
    for {
      amount    <- validateAmount(declaration)
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
