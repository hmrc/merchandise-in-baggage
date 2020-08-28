/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.model.api.PaymentRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core.Declaration
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PaymentServiceSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {

  "persist a declaration from a payment request" in new PaymentService {
    val paymentRequest: PaymentRequest = aPaymentRequest
    val declarationInInitialState = paymentRequest.toDeclarationInInitialState
    val persist: Declaration => Future[Declaration] = _ => Future.successful(declarationInInitialState)
    import paymentRequest._

    whenReady(persistDeclaration(persist, paymentRequest)) { result =>
      result mustBe Declaration(result.declarationId, traderName, amount, csgTpsProviderId,
        chargeReference, result.paymentStatus, result.paid, result.reconciled)
    }
  }
}
