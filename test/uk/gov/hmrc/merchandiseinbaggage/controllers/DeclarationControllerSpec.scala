/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationIdResponse, DeclarationRequest, PaymentStatusRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.mongo.MongoConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DeclarationControllerSpec extends BaseSpecWithApplication with CoreTestData with MongoConfiguration {

  private lazy val component = injector.instanceOf[MessagesControllerComponents]

  "on submit will persist the declaration returning 201 + declaration id" in {
    val declaration = aDeclaration
    setUp(Right(declaration)) { controller =>
      val declarationRequest = aPaymentRequest
      val postRequest = buildPost(routes.DeclarationController.onDeclarations().url).withBody[DeclarationRequest](declarationRequest)
      val eventualResult = controller.onDeclarations()(postRequest)

      status(eventualResult) mustBe 201
      contentAsJson(eventualResult) mustBe Json.toJson(DeclarationIdResponse(declaration.declarationId))
    }
  }

  "on retrieve will return declaration for a given id" in {
    val declaration = aDeclaration
    setUp(Right(declaration)) { controller =>
      val getRequest = buildGet(routes.DeclarationController.onRetrieve(declaration.declarationId.value).url)
      val eventualResult = controller.onRetrieve(declaration.declarationId.value)(getRequest)

      status(eventualResult) mustBe 200
      contentAsJson(eventualResult) mustBe Json.toJson(declaration)
    }
  }

  "on updatePaymentStatus will invoke the service to update the payment status" in {
    val declaration = aDeclaration
    setUp(Right(declaration.withPaidStatus())) { controller =>
      val patchRequest = buildPatch(routes.DeclarationController.onUpdate(declaration.declarationId.value).url)
        .withBody[PaymentStatusRequest](PaymentStatusRequest(Paid))
      val eventualResult = controller.onUpdate(declaration.declarationId.value)(patchRequest)

      status(eventualResult) mustBe 204
    }
  }

  "on updatePaymentStatus will invoke the service to update the payment status if invalid will return 400" in {
    val declaration = aDeclaration
    setUp(Left(InvalidPaymentStatus)) { controller =>
      val patchRequest = buildPatch(routes.DeclarationController.onUpdate(declaration.declarationId.value).url)
        .withBody[PaymentStatusRequest](PaymentStatusRequest(Outstanding))
      val eventualResult = controller.onUpdate(declaration.declarationId.value)(patchRequest)

      status(eventualResult) mustBe 400
    }
  }


  def setUp(stubbedPersistedDeclaration: Either[BusinessError, Declaration])(fn: DeclarationController => Any)(): Unit = {
    val reactiveMongo = new ReactiveMongoComponent { override def mongoConnector: MongoConnector = MongoConnector(mongoConf.uri)}
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)

    val controller = new DeclarationController(component, repository) {
      override def updatePaymentStatus(findByDeclarationId: DeclarationId => Future[Option[Declaration]], updateStatus: (Declaration, PaymentStatus) => Future[Declaration], declarationId: DeclarationId, paymentStatus: PaymentStatus)(implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
        EitherT[Future, BusinessError, Declaration](Future.successful(stubbedPersistedDeclaration))

      override def persistDeclaration(persist: Declaration => Future[Declaration], paymentRequest: DeclarationRequest)
                                     (implicit ec: ExecutionContext): Future[Declaration] = Future.successful(stubbedPersistedDeclaration.right.get)

      override def findByDeclarationId(findById: DeclarationId => Future[Option[Declaration]], declarationId: DeclarationId)
                                      (implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
        EitherT[Future, BusinessError, Declaration](Future.successful(stubbedPersistedDeclaration))
    }

    fn(controller)
  }
}
