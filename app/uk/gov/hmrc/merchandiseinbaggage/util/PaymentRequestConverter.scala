/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.util

import java.util.UUID

import uk.gov.hmrc.merchandiseinbaggage.model.api.PaymentRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Declaration, DeclarationId, Outstanding}

trait PaymentRequestConverter {


  def toDeclaration(paymentRequest: PaymentRequest) = {
    import paymentRequest._
    Declaration(DeclarationId(UUID.randomUUID().toString), traderName, amount, csgTpsProviderId, chargeReference, Outstanding, None, None)
  }
}
