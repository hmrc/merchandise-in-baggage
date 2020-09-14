/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.api

import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.model.core.Declaration
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class DeclarationRequestSpec extends BaseSpec with CoreTestData {

  "Serialise/Deserialise from/to json to PaymentRequest" in {
    val paymentRequest = aPaymentRequest
    val actual = Json.toJson(paymentRequest).toString

    Json.toJson(paymentRequest) mustBe Json.parse(actual)
  }

  "convert a payment request in to an outstanding declaration with no recorded payments and reconciliation" in {
    val actualDeclaration: Declaration = aPaymentRequest.toDeclarationInInitialState

    actualDeclaration must matchPattern { case Declaration(_, _, _, _, _) => }
  }
}
