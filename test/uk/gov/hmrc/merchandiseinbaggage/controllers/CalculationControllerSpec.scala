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

import java.time.LocalDate

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.ConversionRatePeriod
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationRequest, CalculationResult}
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CalculationControllerSpec extends BaseSpecWithApplication with CoreTestData {

  val today = LocalDate.now
  val period = ConversionRatePeriod(today, today, "EUR", BigDecimal(1.1))
  val expectedResult = CalculationResult(aImportGoods, 10000.toAmountInPence, 0.toAmountInPence, 2000.toAmountInPence, Some(period))
  val connector = injector.instanceOf[CurrencyConversionConnector]
  val service = new CalculationService(connector) {
    override def calculate(calculationRequests: CalculationRequest)(implicit hc: HeaderCarrier): Future[CalculationResult] =
      Future.successful(expectedResult)
  }

  "handle multiple calculation requests" in {
    val controller = new CalculationController(service, component)
    val calculationRequests = Seq(CalculationRequest(aImportGoods))

    val request = buildPost(routes.CalculationController.handleCalculation().url)
      .withBody[Seq[CalculationRequest]](calculationRequests)
    val eventualResult = controller.handleCalculations(request)

    status(eventualResult) mustBe 200
    contentAsJson(eventualResult) mustBe Json.toJson(Seq(expectedResult))
  }
}
