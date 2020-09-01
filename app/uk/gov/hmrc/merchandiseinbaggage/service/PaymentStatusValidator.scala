/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import cats.Id
import cats.data.EitherT
import uk.gov.hmrc.merchandiseinbaggage.model.core._

trait PaymentStatusValidator {

  def validateNewStatus(declaration: Declaration, newStatus: PaymentStatus): EitherT[Id, InvalidPaymentStatus.type, Declaration] =
    EitherT.fromOption(validPaymentStatusUpdates.get((declaration.paymentStatus, newStatus)), InvalidPaymentStatus)
      .map(status => declaration.copy(paymentStatus = status))

  private val validPaymentStatusUpdates: Map[(PaymentStatus, PaymentStatus), PaymentStatus] = Map(
    (Outstanding, Paid)       -> Paid,
    (Outstanding, Reconciled) -> Reconciled
  )
}
