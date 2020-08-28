/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.PaymentRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core.Declaration
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
    val postRequest = buildPost(routes.PaymentController.onPayment().url).withJsonBody(requestBody)
    val eventualResult = controller.onPayment()(postRequest)

    status(eventualResult) mustBe 201
    contentAsJson(eventualResult) mustBe Json.toJson(declaration.declarationId)
  }
}
