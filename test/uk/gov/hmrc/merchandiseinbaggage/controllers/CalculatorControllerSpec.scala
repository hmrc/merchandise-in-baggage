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

import java.time.LocalDate

import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.{CalculationRequest, CurrencyConversionResponse}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CalculatorControllerSpec extends BaseSpecWithApplication with CoreTestData with MongoConfiguration with ScalaFutures {

  private lazy val component = injector.instanceOf[MessagesControllerComponents]

  "will trigger customs duty calculation" in {
    val controller = new CalculatorController(
      component,
      (currency: String, _: LocalDate) => Future successful List(CurrencyConversionResponse(currency, Some("0.01"))))

    val postRequest = buildPost(routes.CalculatorController.onCalculations().url)
      .withBody[CalculationRequest](aCalculationRequest).withHeaders(CONTENT_TYPE -> JSON)

    val eventualResult = controller.onCalculations()(postRequest)
    status(eventualResult) mustBe 200
    contentAsString(eventualResult) mustBe "10"
  }

  "will return not found if currency conversion do not exists" in {
    val controller =
      new CalculatorController(component, (_: String, _: LocalDate) => Future successful List.empty)
    val postRequest = buildPost(routes.CalculatorController.onCalculations().url)
      .withBody[CalculationRequest](aCalculationRequest).withHeaders(CONTENT_TYPE -> JSON)

    val eventualResult = controller.onCalculations()(postRequest)
    status(eventualResult) mustBe 404
  }

  "will return 500 if currency conversion service call fails" in {
    val controller =
      new CalculatorController(component, (_: String, _: LocalDate) => Future.failed(new RuntimeException))
    val postRequest = buildPost(routes.CalculatorController.onCalculations().url)
      .withBody[CalculationRequest](aCalculationRequest).withHeaders(CONTENT_TYPE -> JSON)

    val eventualResult = controller.onCalculations()(postRequest)
    status(eventualResult) mustBe 500
  }
}
