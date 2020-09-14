/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import cats.data.EitherT
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
    val actual: EitherT[Future, BusinessError, Declaration] = persistDeclaration(persist, paymentRequest)

    whenReady(actual.value) { res =>
      val result = res.right.get
      result mustBe Declaration(result.declarationId, traderName, amount, csgTpsProviderId, chargeReference)
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
}
