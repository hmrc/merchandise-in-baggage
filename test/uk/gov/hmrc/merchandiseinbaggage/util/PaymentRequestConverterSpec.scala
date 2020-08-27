/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.util

import uk.gov.hmrc.merchandiseinbaggage.model.core.{Declaration, Outstanding}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class PaymentRequestConverterSpec extends BaseSpec with CoreTestData {

  "convert a payment request in to an outstanding declaration with no recorded payments and reconciliation" in new PaymentRequestConverter {
    val actualDeclaration: Declaration = toDeclaration(aPaymentRequest)

    actualDeclaration must matchPattern { case Declaration(_, _, _, _, _, _, _, _) => }
    actualDeclaration.paymentStatus mustBe Outstanding
    actualDeclaration.paid mustBe empty
    actualDeclaration.reconciled mustBe empty
  }
}
