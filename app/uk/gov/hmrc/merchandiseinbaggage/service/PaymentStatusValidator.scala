package uk.gov.hmrc.merchandiseinbaggage.service

import uk.gov.hmrc.merchandiseinbaggage.model.core.{BusinessError, Declaration, InvalidPaymentStatus, Outstanding, Paid, PaymentStatus, Reconciled}

trait PaymentStatusValidator {

  def validateNewStatus(declaration: Declaration, newStatus: PaymentStatus): Either[BusinessError, Declaration] =
    (declaration.paymentStatus, newStatus) match {
      case (Outstanding, Paid | Reconciled) => Right(declaration.copy(paymentStatus = newStatus))
      case (Outstanding, Outstanding)       => Left(InvalidPaymentStatus)
    }
}
