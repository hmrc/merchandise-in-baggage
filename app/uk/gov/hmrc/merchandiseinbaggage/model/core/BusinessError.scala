package uk.gov.hmrc.merchandiseinbaggage.model.core

sealed trait BusinessError
case object InvalidPaymentStatus extends BusinessError
