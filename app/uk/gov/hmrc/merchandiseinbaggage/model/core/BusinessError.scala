/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.core

sealed trait BusinessError
case object InvalidPaymentStatus extends BusinessError
case object DeclarationNotFound extends BusinessError