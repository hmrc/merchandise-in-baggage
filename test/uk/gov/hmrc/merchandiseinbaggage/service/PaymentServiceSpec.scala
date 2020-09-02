/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.model.api.PaymentRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Declaration, DeclarationId, DeclarationNotFound, InvalidPaymentStatus, Outstanding, Paid, PaymentStatus}
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

  "find a declaration by id or returns not found" in new PaymentService {
    val declaration = aDeclaration
    val stubbedFind: DeclarationId => Future[Option[Declaration]] = _ => Future.successful(Some(declaration))
    val stubbedNotFound: DeclarationId => Future[Option[Declaration]] = _ => Future.successful(None)

    whenReady(findByDeclarationId(stubbedFind, declaration.declarationId).value) { result =>
      result mustBe Right(declaration)
    }

    whenReady(findByDeclarationId(stubbedNotFound, declaration.declarationId).value) { result =>
      result mustBe Left(DeclarationNotFound)
    }
  }

  "update a declaration payment status" in new PaymentService {
    val declarationInInitialState = aDeclaration.copy(paymentStatus = Outstanding)
    val newStatus: PaymentStatus = Paid
    val updatedDeclaration: Declaration = declarationInInitialState.copy(paymentStatus = newStatus)
    val findByDeclarationId: DeclarationId => Future[Option[Declaration]] = _ => Future.successful(Option(declarationInInitialState))
    val updateStatus: (Declaration, PaymentStatus) => Future[Declaration] = (_, _) => Future(updatedDeclaration)

    whenReady(updatePaymentStatus(findByDeclarationId, updateStatus, declarationInInitialState.declarationId, newStatus).value) { result =>
      result mustBe Right(updatedDeclaration)
    }
  }

  "fail to update a declaration payment status if invalid" in new PaymentService {
    val outstandingDeclaration = aDeclaration.copy(paymentStatus = Outstanding)
    val invalidStatus: PaymentStatus = Outstanding
    val updatedDeclaration: Declaration = outstandingDeclaration.copy(paymentStatus = invalidStatus)
    val findByDeclarationId: DeclarationId => Future[Option[Declaration]] = _ => Future.successful(Option(outstandingDeclaration))
    val updateStatus: (Declaration, PaymentStatus) => Future[Declaration] = (_, _) => Future(updatedDeclaration)

    whenReady(updatePaymentStatus(findByDeclarationId, updateStatus, outstandingDeclaration.declarationId, invalidStatus).value) { result =>
      result mustBe Left(InvalidPaymentStatus)
    }
  }
}
