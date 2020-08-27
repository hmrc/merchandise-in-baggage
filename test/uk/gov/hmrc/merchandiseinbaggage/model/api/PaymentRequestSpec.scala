/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Declaration, Outstanding}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithMongoTestServer, CoreTestData}

class PaymentRequestSpec extends BaseSpecWithMongoTestServer with CoreTestData {

  "Serialise/Deserialise from/to json to PaymentRequest" in {
    val paymentRequest = aPaymentRequest
    val actual = Json.toJson(paymentRequest).toString

    Json.toJson(paymentRequest) mustBe Json.parse(actual)
  }

  "convert a payment request in to an outstanding declaration with no recorded payments and reconciliation" in {
    val actualDeclaration: Declaration = aPaymentRequest.toDeclarationInInitialState

    actualDeclaration must matchPattern { case Declaration(_, _, _, _, _, _, _, _) => }
    actualDeclaration.paymentStatus mustBe Outstanding
    actualDeclaration.paid mustBe empty
    actualDeclaration.reconciled mustBe empty
  }
}
