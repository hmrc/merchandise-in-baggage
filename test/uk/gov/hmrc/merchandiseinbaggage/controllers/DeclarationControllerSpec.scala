/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggage.connectors.EmailConnector
import uk.gov.hmrc.merchandiseinbaggage.model.DeclarationEmailInfo
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, MibReference}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepositoryImpl
import uk.gov.hmrc.merchandiseinbaggage.service.{DeclarationService, EmailService}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.mongo.MongoConnector
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DeclarationControllerSpec extends BaseSpecWithApplication with CoreTestData with MongoConfiguration {

  private lazy val component = injector.instanceOf[MessagesControllerComponents]

  "on submit will persist the declaration returning 201 + declaration id" in {
    val declaration = aDeclaration
    setUp(Right(declaration)) { controller =>
      val postRequest = buildPost(routes.DeclarationController.onDeclarations().url).withBody[Declaration](declaration)
      val eventualResult = controller.onDeclarations()(postRequest)

      status(eventualResult) mustBe 201
      contentAsJson(eventualResult) mustBe Json.toJson(declaration.declarationId)
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

  "sendEmail should response as expected" in {
    val declaration = aDeclaration
    setUp(Right(declaration)) { controller =>
      val postRequest = buildPost(routes.DeclarationController.sendEmails(declaration.declarationId.value).url)
      val eventualResult = controller.sendEmails(declaration.declarationId.value)(postRequest)

      status(eventualResult) mustBe 202
    }
  }

  "/payment-callback should trigger email delivery and update paymentSuccess flag" in {
    val declaration = aDeclaration
    setUp(Right(declaration)) { controller =>
      val postRequest = buildPost(routes.DeclarationController.paymentSuccessCallback(declaration.mibReference.value).url)
      val eventualResult = controller.paymentSuccessCallback(declaration.mibReference.value)(postRequest)

      status(eventualResult) mustBe 200
    }
  }

  def setUp(stubbedPersistedDeclaration: Either[BusinessError, Declaration])(fn: DeclarationController => Any)(): Unit = {
    val reactiveMongo = new ReactiveMongoComponent {
      override def mongoConnector: MongoConnector = MongoConnector(mongoConf.uri)
    }

    val repository = new DeclarationRepositoryImpl(reactiveMongo.mongoConnector.db)
    val auditConnector = injector.instanceOf[AuditConnector]

    val emailConnector = new EmailConnector {
      override def sendEmails(emailInformation: DeclarationEmailInfo)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Int] =
        Future.successful(202)
    }

    val emailService = new EmailService(emailConnector, repository)

    val declarationService = new DeclarationService(repository, emailService, auditConnector) {
      override def persistDeclaration(paymentRequest: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Declaration] =
        Future.successful(stubbedPersistedDeclaration.right.get)

      override def upsertDeclaration(declaration: Declaration)(implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
        EitherT[Future, BusinessError, Declaration](Future.successful(stubbedPersistedDeclaration))

      override def findByDeclarationId(declarationId: DeclarationId)(
        implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
        EitherT[Future, BusinessError, Declaration](Future.successful(stubbedPersistedDeclaration))

      override def findByMibReference(mibReference: MibReference)(
        implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
        EitherT[Future, BusinessError, Declaration](Future.successful(stubbedPersistedDeclaration))
    }

    val controller = new DeclarationController(declarationService, component)

    fn(controller)
  }
}
