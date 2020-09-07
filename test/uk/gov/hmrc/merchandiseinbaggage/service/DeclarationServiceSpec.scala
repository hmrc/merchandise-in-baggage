/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationServiceSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {

  "persist a declaration from a payment request" in new DeclarationService {
    val paymentRequest: DeclarationRequest = aPaymentRequest
    val declarationInInitialState: Declaration = paymentRequest.toDeclarationInInitialState
    val persist: Declaration => Future[Declaration] = _ => Future.successful(declarationInInitialState)
    import paymentRequest._

    whenReady(persistDeclaration(persist, paymentRequest)) { result =>
      result mustBe Declaration(result.declarationId, traderName, amount, csgTpsProviderId,
        chargeReference, result.paymentStatus, result.paid, result.reconciled)
    }
  }

  "find a declaration by id or returns not found" in new DeclarationService {
    val declaration: Declaration = aDeclaration
    val stubbedFind: DeclarationId => Future[Option[Declaration]] = _ => Future.successful(Some(declaration))
    val stubbedNotFound: DeclarationId => Future[Option[Declaration]] = _ => Future.successful(None)

    whenReady(findByDeclarationId(stubbedFind, declaration.declarationId).value) { result =>
      result mustBe Right(declaration)
    }

    whenReady(findByDeclarationId(stubbedNotFound, declaration.declarationId).value) { result =>
      result mustBe Left(DeclarationNotFound)
    }
  }

  "update a declaration payment status and add time" in new DeclarationService {
    val withStatusUpdate = new AtomicBoolean(false)
    val declarationInInitialState: Declaration = aDeclaration.copy(paymentStatus = Outstanding)
    val newStatus: PaymentStatus = Paid
    val updatedDeclaration: Declaration = declarationInInitialState.copy(paymentStatus = newStatus)
    val findByDeclarationId: DeclarationId => Future[Option[Declaration]] = _ => Future.successful(Option(declarationInInitialState))
    val updateStatus: (Declaration, PaymentStatus) => Future[Declaration] = (_, _) => Future(updatedDeclaration)

    override protected def statusUpdateTime(paymentStatus: PaymentStatus, declaration: Declaration): Declaration = {
      withStatusUpdate.set(true)
      updatedDeclaration
    }

    whenReady(updatePaymentStatus(findByDeclarationId, updateStatus, declarationInInitialState.declarationId, newStatus).value) { result =>
      result mustBe Right(updatedDeclaration)
      withStatusUpdate.get() mustBe true
    }
  }

  "fail to update a declaration payment status if invalid" in new DeclarationService {
    val outstandingDeclaration: Declaration = aDeclaration.copy(paymentStatus = Outstanding)
    val invalidStatus: PaymentStatus = Outstanding
    val updatedDeclaration: Declaration = outstandingDeclaration.copy(paymentStatus = invalidStatus)
    val findByDeclarationId: DeclarationId => Future[Option[Declaration]] = _ => Future.successful(Option(outstandingDeclaration))
    val updateStatus: (Declaration, PaymentStatus) => Future[Declaration] = (_, _) => Future(updatedDeclaration)

    whenReady(updatePaymentStatus(findByDeclarationId, updateStatus, outstandingDeclaration.declarationId, invalidStatus).value) { result =>
      result mustBe Left(InvalidPaymentStatus)
    }
  }

  "generate time for valid updates or None if invalid" in new DeclarationService {
    val outstandingDeclaration: Declaration = aDeclaration.copy(paymentStatus = Outstanding)
    val now: LocalDateTime = LocalDateTime.now
    override protected def generateTime: LocalDateTime = now

    statusUpdateTime(Paid, outstandingDeclaration).paid mustBe Some(now)
    statusUpdateTime(Reconciled, outstandingDeclaration).reconciled mustBe Some(now)
    statusUpdateTime(Outstanding, outstandingDeclaration).paid mustBe None
    statusUpdateTime(Outstanding, outstandingDeclaration).reconciled mustBe None
  }
}
