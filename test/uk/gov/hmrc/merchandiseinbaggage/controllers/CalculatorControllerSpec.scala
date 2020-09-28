/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import cats.data.EitherT
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.CalculationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core.{AmountInPence, BusinessError, CurrencyNotFound}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CalculatorControllerSpec extends BaseSpecWithApplication with CoreTestData with MongoConfiguration with ScalaFutures {

  private lazy val component = injector.instanceOf[MessagesControllerComponents]
  private lazy val client = injector.instanceOf[HttpClient]

  "will trigger customs duty calculation" in {
    val expectedValue = "1.22"

    val controller = new CalculatorController(component, client) {
      override def customDuty(httpClient: HttpClient, calculationRequest: CalculationRequest)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, BusinessError, AmountInPence] =
        EitherT[Future, BusinessError, AmountInPence](Future.successful(Right(AmountInPence(expectedValue.toDouble))))
    }
    val postRequest = buildPost(routes.CalculatorController.onCalculations().url)
      .withBody[CalculationRequest](aCalculationRequest).withHeaders(CONTENT_TYPE -> JSON)

    val eventualResult = controller.onCalculations()(postRequest)
    status(eventualResult) mustBe 200
    contentAsString(eventualResult) mustBe expectedValue
  }

  "will return not found if currency conversion do not exists" in {
    val calculationRequest = aCalculationRequest
    val requestBody = Json.toJson(calculationRequest)

    val controller = new CalculatorController(component, client) {
      override def customDuty(httpClient: HttpClient, calculationRequest: CalculationRequest)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, BusinessError, AmountInPence] =
        EitherT[Future, BusinessError, AmountInPence](Future.successful(Left(CurrencyNotFound)))
    }
    val postRequest = buildPost(routes.CalculatorController.onCalculations().url)
      .withBody[CalculationRequest](aCalculationRequest).withHeaders(CONTENT_TYPE -> JSON)

    val eventualResult = controller.onCalculations()(postRequest)
    status(eventualResult) mustBe 404
  }

  "will return 500 if currency conversion service call fails" in {
    val controller = new CalculatorController(component, client) {
      override def customDuty(httpClient: HttpClient, calculationRequest: CalculationRequest)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, BusinessError, AmountInPence] =
        EitherT[Future, BusinessError, AmountInPence](Future.failed(new Exception))
    }
    val postRequest = buildPost(routes.CalculatorController.onCalculations().url)
      .withBody[CalculationRequest](aCalculationRequest).withHeaders(CONTENT_TYPE -> JSON)

    val eventualResult = controller.onCalculations()(postRequest)
    status(eventualResult) mustBe 500
  }
}
