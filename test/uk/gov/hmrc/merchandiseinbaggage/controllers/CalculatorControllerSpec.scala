/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{contentAsJson, status, _}
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global

class CalculatorControllerSpec extends BaseSpecWithApplication with CoreTestData with MongoConfiguration {

  private lazy val component = injector.instanceOf[MessagesControllerComponents]

  "will trigger customs duty calculation" in {
    val calculationRequest = aCalculationRequest
    val requestBody = Json.toJson(calculationRequest)
    val controller = new CalculatorController(component)
    val postRequest = buildPost(routes.CalculatorController.onCalculations().url)
      .withJsonBody(requestBody)
      .withHeaders("Content-Type" -> "application/json")
    val eventualResult = controller.onCalculations()(postRequest)

    status(eventualResult) mustBe 200
    contentAsString(eventualResult) mustBe "0.1"
  }
}
