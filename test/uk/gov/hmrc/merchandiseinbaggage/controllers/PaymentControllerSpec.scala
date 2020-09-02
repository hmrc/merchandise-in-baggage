/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import cats.data.EitherT
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationIdResponse, PaymentRequest, PaymentStatusRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{BusinessError, Declaration, DeclarationId, InvalidPaymentStatus, Outstanding, Paid, PaymentStatus}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.mongo.MongoConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PaymentControllerSpec extends BaseSpecWithApplication with CoreTestData with MongoConfiguration {

  private lazy val component = app.injector.instanceOf[MessagesControllerComponents]

  "on submit will trigger a call to pay-api and render the response" in {
    val reactiveMongo = new ReactiveMongoComponent { override def mongoConnector: MongoConnector = MongoConnector(mongoConf.uri)}
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)
    val declaration = aDeclaration
    val controller = new PaymentController(component, repository) {
      override def persistDeclaration(persist: Declaration => Future[Declaration], paymentRequest: PaymentRequest)
                                     (implicit ec: ExecutionContext): Future[Declaration] = Future.successful(declaration)
    }
    val paymentRequest = aPaymentRequest
    val requestBody = Json.toJson(paymentRequest)
    val postRequest = buildPost(routes.PaymentController.onPayments().url).withJsonBody(requestBody)
    val eventualResult = controller.onPayments()(postRequest)

    status(eventualResult) mustBe 201
    contentAsJson(eventualResult) mustBe Json.toJson(DeclarationIdResponse(declaration.declarationId))
  }

  "on updatePaymentStatus will invoke the service to update the payment status" in {
    val declaration = aDeclaration
    val reactiveMongo = new ReactiveMongoComponent { override def mongoConnector: MongoConnector = MongoConnector(mongoConf.uri)}
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)
    val controller = new PaymentController(component, repository) {
      override def updatePaymentStatus(findByDeclarationId: DeclarationId => Future[Option[Declaration]], updateStatus: (Declaration, PaymentStatus) => Future[Declaration], declarationId: DeclarationId, paymentStatus: PaymentStatus)(implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
        EitherT[Future, BusinessError, Declaration](Future.successful(Right(declaration.withPaidStatus())))
    }
    val patchRequest = buildPatch(routes.PaymentController.onUpdate(declaration.declarationId.value).url)
      .withJsonBody(Json.toJson(PaymentStatusRequest(Paid)))
    val eventualResult = controller.onUpdate(declaration.declarationId.value)(patchRequest)

    status(eventualResult) mustBe 204
  }

  "on updatePaymentStatus will invoke the service to update the payment status if invalid will return 400" in {
    val declaration = aDeclaration
    val reactiveMongo = new ReactiveMongoComponent { override def mongoConnector: MongoConnector = MongoConnector(mongoConf.uri)}
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)
    val controller = new PaymentController(component, repository) {
      override def updatePaymentStatus(findByDeclarationId: DeclarationId => Future[Option[Declaration]], updateStatus: (Declaration, PaymentStatus) => Future[Declaration], declarationId: DeclarationId, paymentStatus: PaymentStatus)(implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
        EitherT[Future, BusinessError, Declaration](Future.successful(Left(InvalidPaymentStatus)))
    }

    val patchRequest = buildPatch(routes.PaymentController.onUpdate(declaration.declarationId.value).url)
      .withJsonBody(Json.toJson(PaymentStatusRequest(Outstanding)))
    val eventualResult = controller.onUpdate(declaration.declarationId.value)(patchRequest)

    status(eventualResult) mustBe 400
  }
}
