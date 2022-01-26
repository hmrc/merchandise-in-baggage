/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.{LocalDate, LocalDateTime}

import cats.data.OptionT
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, ConversionRatePeriod, DeclarationGoods}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation._
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CalculationControllerSpec extends BaseSpecWithApplication with CoreTestData with MockFactory {

  val today = LocalDate.now
  val period = ConversionRatePeriod(today, today, "EUR", BigDecimal(1.1))
  val expectedResult = CalculationResult(aImportGoods, 10000.toAmountInPence, 0.toAmountInPence, 2000.toAmountInPence, Some(period))
  val mockService = mock[CalculationService]

  s"handle multiple calculation requests delegating to CalculationService" in {
    val controller = new CalculationController(mockService, component)
    val calculationRequests = Seq(CalculationRequest(aImportGoods, GreatBritain))

    (mockService
      .calculate(_: Seq[CalculationRequest])(_: HeaderCarrier))
      .expects(calculationRequests, *)
      .returning(Future.successful(CalculationResponse(CalculationResults(Seq(expectedResult)), WithinThreshold)))

    val request = buildPost(CalculationController.handleCalculations().url)
      .withBody[Seq[CalculationRequest]](calculationRequests)
    val eventualResult = controller.handleCalculations(request)

    status(eventualResult) mustBe 200
    contentAsJson(eventualResult) mustBe Json.toJson(CalculationResponse(CalculationResults(Seq(expectedResult)), WithinThreshold))
  }

  s"handle amends calculations delegating to CalculationService" in {
    val controller = new CalculationController(mockService, component)
    val amend = Amendment(111, LocalDateTime.now, DeclarationGoods(Seq(aImportGoods)))
    val calculationRequest = CalculationAmendRequest(Some(amend), Some(GreatBritain), aDeclarationId)

    (mockService
      .calculateAmendPlusOriginal(_: CalculationAmendRequest)(_: HeaderCarrier))
      .expects(calculationRequest, *)
      .returning(OptionT.pure[Future](CalculationResponse(CalculationResults(Seq(expectedResult)), WithinThreshold)))

    val request = buildPost(CalculationController.handleAmendCalculations().url)
      .withBody[CalculationAmendRequest](calculationRequest)
    val eventualResult = controller.handleAmendCalculations(request)

    status(eventualResult) mustBe 200
    contentAsJson(eventualResult) mustBe Json.toJson(CalculationResponse(CalculationResults(Seq(expectedResult)), WithinThreshold))
  }
}
