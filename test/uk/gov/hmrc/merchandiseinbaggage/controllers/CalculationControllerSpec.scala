/*
 * Copyright 2025 HM Revenue & Customs
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

import cats.data.OptionT
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import play.api.libs.json.Json
import play.api.test.Helpers.*
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes.*
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.*
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, ConversionRatePeriod, DeclarationGoods}
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.Future

class CalculationControllerSpec extends BaseSpecWithApplication with CoreTestData {

  val today          = LocalDate.now
  val period         = ConversionRatePeriod(today, today, "EUR", BigDecimal(1.1))
  val expectedResult =
    CalculationResult(aImportGoods, 10000.toAmountInPence, 0.toAmountInPence, 2000.toAmountInPence, Some(period))
  val mockService    = mock(classOf[CalculationService])

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockService)
  }

  s"handle multiple calculation requests delegating to CalculationService" in {
    val controller          = new CalculationController(mockService, component)
    val calculationRequests = Seq(CalculationRequest(aImportGoods, GreatBritain))

    when(mockService.calculate(any())(any()))
      .thenReturn(Future.successful(CalculationResponse(CalculationResults(Seq(expectedResult)), WithinThreshold)))

    val request        = buildPost(CalculationController.handleCalculations().url)
      .withBody[Seq[CalculationRequest]](calculationRequests)
    val eventualResult = controller.handleCalculations(request)

    status(eventualResult) mustBe 200
    contentAsJson(eventualResult) mustBe Json.toJson(
      CalculationResponse(CalculationResults(Seq(expectedResult)), WithinThreshold)
    )
  }

  s"handle amends calculations delegating to CalculationService" in {
    val controller         = new CalculationController(mockService, component)
    val amend              = Amendment(111, LocalDateTime.now.truncatedTo(ChronoUnit.MILLIS), DeclarationGoods(Seq(aImportGoods)))
    val calculationRequest = CalculationAmendRequest(Some(amend), Some(GreatBritain), aDeclarationId)

    when(mockService.calculateAmendPlusOriginal(any())(any()))
      .thenReturn(OptionT.pure[Future](CalculationResponse(CalculationResults(Seq(expectedResult)), WithinThreshold)))

    val request        = buildPost(CalculationController.handleAmendCalculations().url)
      .withBody[CalculationAmendRequest](calculationRequest)
    val eventualResult = controller.handleAmendCalculations(request)

    status(eventualResult) mustBe 200
    contentAsJson(eventualResult) mustBe Json.toJson(
      CalculationResponse(CalculationResults(Seq(expectedResult)), WithinThreshold)
    )
  }
}
