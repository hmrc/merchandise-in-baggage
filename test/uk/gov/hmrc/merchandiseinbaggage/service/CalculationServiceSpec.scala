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

package uk.gov.hmrc.merchandiseinbaggage.service

import java.time.LocalDate.now
import java.time.{LocalDate, LocalDateTime}

import cats.data.EitherT
import com.softwaremill.quicklens._
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation._
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CalculationServiceSpec extends BaseSpecWithApplication with ScalaFutures with MockFactory with CoreTestData {

  val connector: CurrencyConversionConnector = mock[CurrencyConversionConnector]
  val mockDeclarationService: DeclarationService = mock[DeclarationService]
  val service = new CalculationService(connector, mockDeclarationService)

  "convert currency and calculate duty and vat for an item from outside the EU" in {
    val period = ConversionRatePeriod(now(), now(), "USD", BigDecimal(1.1))
    (connector
      .getConversionRate(_: String, _: LocalDate)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(
        Future.successful(Seq(period))
      )

    val importGoods = aImportGoods
      .modify(_.producedInEu)
      .setTo(YesNoDontKnow.No)
      .modify(_.purchaseDetails.currency.code)
      .setTo("USD")
      .modify(_.purchaseDetails.currency.valueForConversion.each)
      .setTo("USD")

    val calculationResult = CalculationResult(importGoods, AmountInPence(9091), AmountInPence(300), AmountInPence(470), Some(period))

    service
      .calculate(Seq(CalculationRequest(importGoods, GreatBritain)))
      .futureValue
      .results
      .calculationResults
      .head mustBe calculationResult
  }

  "convert currency and calculate duty and vat for an item where origin is unknown" in {
    val period = ConversionRatePeriod(now(), now(), "USD", BigDecimal(1.1))
    (connector
      .getConversionRate(_: String, _: LocalDate)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(
        Future.successful(Seq(period))
      )

    val importGoods = aImportGoods
      .modify(_.producedInEu)
      .setTo(YesNoDontKnow.DontKnow)
      .modify(_.purchaseDetails.currency.code)
      .setTo("USD")
      .modify(_.purchaseDetails.currency.valueForConversion.each)
      .setTo("USD")

    val calculationResult = CalculationResult(importGoods, AmountInPence(9091), AmountInPence(300), AmountInPence(470), Some(period))

    service
      .calculate(Seq(CalculationRequest(importGoods, GreatBritain)))
      .futureValue
      .results
      .calculationResults
      .head mustBe calculationResult
  }

  "convert currency and calculate duty and vat for an item from inside the EU" in {
    val period = ConversionRatePeriod(now(), now(), "EUR", BigDecimal(1.1))
    (connector
      .getConversionRate(_: String, _: LocalDate)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(
        Future.successful(Seq(period))
      )

    val importGoods = aImportGoods
      .modify(_.producedInEu)
      .setTo(YesNoDontKnow.Yes)
      .modify(_.purchaseDetails.currency.code)
      .setTo("EUR")
      .modify(_.purchaseDetails.currency.valueForConversion.each)
      .setTo("EUR")

    val calculationResult = CalculationResult(importGoods, AmountInPence(9091), AmountInPence(0), AmountInPence(455), Some(period))

    service
      .calculate(Seq(CalculationRequest(importGoods, GreatBritain)))
      .futureValue
      .results
      .calculationResults
      .head mustBe calculationResult
  }

  "calculate duty and vat for an item from a country that uses a GBP 1:1 currency" in {
    val importGoods = aImportGoods
      .modify(_.producedInEu)
      .setTo(YesNoDontKnow.Yes)
      .modify(_.purchaseDetails.currency.code)
      .setTo("EUR")
      .modify(_.purchaseDetails.currency.valueForConversion)
      .setTo(Option.empty)

    val calculationResult = CalculationResult(importGoods, AmountInPence(10000), AmountInPence(0), AmountInPence(500), None)

    service
      .calculate(Seq(CalculationRequest(importGoods, GreatBritain)))
      .futureValue
      .results
      .calculationResults
      .head mustBe calculationResult
  }

  s"calculate $ThresholdCheck" in {
    val importGoods = aImportGoods
    val results: Seq[CalculationResult] =
      Seq(
        CalculationResult(importGoods, AmountInPence(100000), AmountInPence(0), AmountInPence(0), None),
        CalculationResult(importGoods, AmountInPence(50000), AmountInPence(0), AmountInPence(0), None)
      )

    service.calculateThresholdImport(results, Some(GreatBritain)) mustBe WithinThreshold
    service.calculateThresholdImport(results.modify(_.each.gbpAmount.value).setTo(150001), Some(GreatBritain)) mustBe OverThreshold
  }

  s"calculate $ThresholdCheck for Export goods" in {
    val declarationGoods = Seq(aExportGoods)
    val declarationGoodsOverThreshold = Seq(aExportGoods.modify(_.purchaseDetails.amount).setTo("1501"))

    service.calculateThresholdExport(declarationGoods, Some(GreatBritain)) mustBe WithinThreshold
    service.calculateThresholdExport(declarationGoodsOverThreshold, Some(GreatBritain)) mustBe OverThreshold
  }

  s"handle $CalculationResults for Export goods" in {
    val requests = Seq(CalculationRequest(aExportGoods, GreatBritain))

    service.calculate(requests).futureValue mustBe CalculationResponse(CalculationResults(Seq.empty), WithinThreshold)
  }

  "handle multiple calculation requests" in {
    val importGoods = aImportGoods
      .modify(_.producedInEu)
      .setTo(YesNoDontKnow.No)
      .modify(_.purchaseDetails.currency.code)
      .setTo("USD")
      .modify(_.purchaseDetails.currency.valueForConversion.each)
      .setTo("USD")

    val calculationRequests = Seq(CalculationRequest(importGoods, GreatBritain), CalculationRequest(importGoods, GreatBritain))
    val period = ConversionRatePeriod(now(), now(), "USD", BigDecimal(1.1))

    (connector
      .getConversionRate(_: String, _: LocalDate)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(
        Future.successful(Seq(period))
      )
      .twice()

    val eventualResult = service.calculate(calculationRequests)
    val expectedResults = Seq(
      CalculationResult(importGoods, 9091.toAmountInPence, 300.toAmountInPence, 470.toAmountInPence, Some(period)),
      CalculationResult(importGoods, 9091.toAmountInPence, 300.toAmountInPence, 470.toAmountInPence, Some(period))
    )

    eventualResult.futureValue mustBe CalculationResponse(CalculationResults(expectedResults), WithinThreshold)
  }

  s"handle multiple calculation requests returning $CalculationResults $OverThreshold" in {
    val importGoods = aImportGoods
      .modify(_.producedInEu)
      .setTo(YesNoDontKnow.No)
      .modify(_.purchaseDetails.currency.code)
      .setTo("USD")
      .modify(_.purchaseDetails.currency.valueForConversion.each)
      .setTo("USD")
      .modify(_.purchaseDetails.amount)
      .setTo("75100")

    val calculationRequests = Seq(CalculationRequest(importGoods, GreatBritain), CalculationRequest(importGoods, GreatBritain))
    val period = ConversionRatePeriod(now(), now(), "USD", BigDecimal(1.1))

    (connector
      .getConversionRate(_: String, _: LocalDate)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(
        Future.successful(Seq(period))
      )
      .twice()

    val eventualResult = service.calculate(calculationRequests)
    val expectedResults = Seq(
      CalculationResult(importGoods, 6827273.toAmountInPence, 225300.toAmountInPence, 352629.toAmountInPence, Some(period)),
      CalculationResult(importGoods, 6827273.toAmountInPence, 225300.toAmountInPence, 352629.toAmountInPence, Some(period))
    )

    eventualResult.futureValue mustBe CalculationResponse(CalculationResults(expectedResults), OverThreshold)
  }

  s"handle calculation requests for $ExportGoods $WithinThreshold" in {
    val exportGoods = aExportGoods
    val calculationRequests = Seq(CalculationRequest(exportGoods, GreatBritain))
    val eventualResult = service.calculate(calculationRequests)

    eventualResult.futureValue mustBe CalculationResponse(CalculationResults(Seq.empty), WithinThreshold)
  }

  s"handle calculation requests for $ExportGoods $OverThreshold" in {
    val exportGoods = aExportGoods.modify(_.purchaseDetails.amount).setTo("150001")
    val calculationRequests = Seq(CalculationRequest(exportGoods, GreatBritain))
    val eventualResult = service.calculate(calculationRequests)

    eventualResult.futureValue mustBe CalculationResponse(CalculationResults(Seq.empty), OverThreshold)
  }

  s"handle calculation for $Amendment $ImportGoods by adding both goods original + amend" in {
    val conversionRatePeriod = ConversionRatePeriod(now(), now(), "USD", BigDecimal(1.1))
    val importGoods = aImportGoods
      .modify(_.producedInEu)
      .setTo(YesNoDontKnow.No)
      .modify(_.purchaseDetails.currency.valueForConversion.each)
      .setTo("USD")
      .modify(_.purchaseDetails.amount)
      .setTo("100")

    val originalGoods = importGoods.modify(_.purchaseDetails.amount).setTo("200")
    val declaration = aDeclaration.modify(_.declarationGoods.goods).setTo(Seq(originalGoods))

    (mockDeclarationService
      .findByDeclarationId(_: DeclarationId))
      .expects(declaration.declarationId)
      .returning(EitherT.pure(declaration))

    (connector
      .getConversionRate(_: String, _: LocalDate)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returns(
        Future.successful(Seq(conversionRatePeriod))
      )
      .twice()

    val amends = Amendment(111, LocalDateTime.now(), DeclarationGoods(Seq(importGoods)))
    val eventualResult =
      service.calculateAmendPlusOriginal(CalculationAmendRequest(Some(amends), Some(GreatBritain), declaration.declarationId))

    val expectedResults = Seq(
      CalculationResult(importGoods, AmountInPence(9091), AmountInPence(300), AmountInPence(470), Some(conversionRatePeriod)),
      CalculationResult(originalGoods, AmountInPence(18182), AmountInPence(600), AmountInPence(939), Some(conversionRatePeriod))
    )
    val expected = eventualResult.value.futureValue.get

    expected.results mustBe CalculationResults(expectedResults)
    expected.thresholdCheck mustBe WithinThreshold
  }
}
