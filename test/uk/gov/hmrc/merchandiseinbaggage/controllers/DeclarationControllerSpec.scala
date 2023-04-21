/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import cats.data.EitherT
import cats.implicits._
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationId, Eori, MibReference}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{BusinessError, DeclarationNotFound, PaymentCallbackRequest}
import uk.gov.hmrc.merchandiseinbaggage.service.DeclarationService
import uk.gov.hmrc.merchandiseinbaggage.util.Utils.FutureOps
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class DeclarationControllerSpec
    extends BaseSpecWithApplication
    with CoreTestData
    with MongoConfiguration
    with MockFactory {

  private val declarationService = mock[DeclarationService]

  val controller = new DeclarationController(declarationService, component)

  "on submit will persist the declaration returning 201 + declaration id" in {
    val declaration = aDeclaration
    (declarationService
      .persistDeclaration(_: Declaration)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returning(declaration.asFuture)

    val postRequest    = buildPost(routes.DeclarationController.onDeclarations().url).withBody[Declaration](declaration)
    val eventualResult = controller.onDeclarations()(postRequest)

    status(eventualResult) mustBe 201
    contentAsJson(eventualResult) mustBe Json.toJson(declaration.declarationId)
  }

  "amendDeclaration will persist the declaration returning 200 + declaration id" in {
    val declaration = aDeclarationWithAmendment
    (declarationService
      .amendDeclaration(_: Declaration)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returning(declaration.asFuture)

    val postRequest    = buildPut(routes.DeclarationController.amendDeclaration().url).withBody[Declaration](declaration)
    val eventualResult = controller.amendDeclaration()(postRequest)

    status(eventualResult) mustBe 200
    contentAsJson(eventualResult) mustBe Json.toJson(declaration.declarationId)
  }

  "on retrieve"            should {
    "return declaration for a given id" in {
      val declaration = aDeclaration
      (declarationService
        .findByDeclarationId(_: DeclarationId))
        .expects(*)
        .returning(EitherT(declaration.asRight.asFuture))

      val getRequest     = buildGet(routes.DeclarationController.onRetrieve(declaration.declarationId).url)
      val eventualResult = controller.onRetrieve(declaration.declarationId)(getRequest)

      status(eventualResult) mustBe 200
      contentAsJson(eventualResult) mustBe Json.toJson(declaration)
    }

    "return 404 for a given id if declaration is not found in mongo" in {
      val declaration = aDeclaration
      (declarationService
        .findByDeclarationId(_: DeclarationId))
        .expects(*)
        .returning(EitherT(DeclarationNotFound.asInstanceOf[BusinessError].asLeft.asFuture))

      val getRequest     = buildGet(routes.DeclarationController.onRetrieve(declaration.declarationId).url)
      val eventualResult = controller.onRetrieve(declaration.declarationId)(getRequest)

      status(eventualResult) mustBe 404
    }
  }
  "findBy MibRef and Eori" should {
    "return a success response" in {
      val declaration = aDeclaration
      (declarationService
        .findBy(_: MibReference, _: Eori))
        .expects(*, *)
        .returning(EitherT(declaration.asRight.asFuture))

      val getRequest     = buildGet(routes.DeclarationController.findBy(declaration.mibReference, declaration.eori).url)
      val eventualResult = controller.findBy(declaration.mibReference, declaration.eori)(getRequest)

      status(eventualResult) mustBe 200
      contentAsJson(eventualResult) mustBe Json.toJson(declaration)
    }

    "return 404 if not found for a given mibRef and Eori" in {
      val declaration = aDeclaration
      (declarationService
        .findBy(_: MibReference, _: Eori))
        .expects(*, *)
        .returning(EitherT(DeclarationNotFound.asInstanceOf[BusinessError].asLeft.asFuture))

      val getRequest     = buildGet(routes.DeclarationController.findBy(declaration.mibReference, declaration.eori).url)
      val eventualResult = controller.findBy(declaration.mibReference, declaration.eori)(getRequest)

      status(eventualResult) mustBe 404
    }
  }

  "POST /payment-callback" should {
    "return 200 when declaration is found" in {
      val declaration = aDeclaration
      (declarationService
        .processPaymentCallback(_: PaymentCallbackRequest)(_: HeaderCarrier))
        .expects(*, *)
        .returning(EitherT(declaration.asRight.asFuture))

      val postRequest = buildPost(routes.DeclarationController.handlePaymentCallback.url)
        .withBody[PaymentCallbackRequest](PaymentCallbackRequest("XJMB8495682992"))

      val eventualResult = controller.handlePaymentCallback(postRequest)

      status(eventualResult) mustBe 200
    }

    "return 404 when declaration is not found" in {
      (declarationService
        .processPaymentCallback(_: PaymentCallbackRequest)(_: HeaderCarrier))
        .expects(*, *)
        .returning(EitherT(DeclarationNotFound.asInstanceOf[BusinessError].asLeft.asFuture))

      val postRequest = buildPost(routes.DeclarationController.handlePaymentCallback.url)
        .withBody[PaymentCallbackRequest](PaymentCallbackRequest("XJMB8495682992"))

      val eventualResult = controller.handlePaymentCallback(postRequest)

      status(eventualResult) mustBe 404
    }
  }
}
