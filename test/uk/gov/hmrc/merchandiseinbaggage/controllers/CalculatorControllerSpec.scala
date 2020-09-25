/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.CalculationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core.AmountInPence
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CalculatorControllerSpec extends BaseSpecWithApplication with CoreTestData with MongoConfiguration {

  private lazy val component = injector.instanceOf[MessagesControllerComponents]
  private lazy val client = injector.instanceOf[HttpClient]

  "will trigger customs duty calculation" in {
    val calculationRequest = aCalculationRequest
    val requestBody = Json.toJson(calculationRequest)
    val expectedValue = "1.22"

    val controller = new CalculatorController(component, client) {
      override def customDuty(httpClient: HttpClient, calculationRequest: CalculationRequest)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AmountInPence] =
        Future.successful(AmountInPence(expectedValue.toDouble))
    }
    val postRequest = buildPost(routes.CalculatorController.onCalculations().url)
      .withJsonBody(requestBody).withHeaders("Content-Type" -> "application/json")

    val eventualResult = controller.onCalculations()(postRequest)
    status(eventualResult) mustBe 200
    contentAsString(eventualResult) mustBe expectedValue
  }
}
