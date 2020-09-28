/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.core

sealed trait BusinessError
case object InvalidPaymentStatus extends BusinessError
case object DeclarationNotFound extends BusinessError
case object CurrencyNotFound extends BusinessError
case object InvalidAmount extends BusinessError
case object InvalidName extends BusinessError
case object InvalidChargeReference extends BusinessError
