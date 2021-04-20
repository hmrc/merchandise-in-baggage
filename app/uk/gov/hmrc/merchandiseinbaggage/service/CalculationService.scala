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

package uk.gov.hmrc.merchandiseinbaggage.service

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation._
import uk.gov.hmrc.merchandiseinbaggage.util.DataModelEnriched._

import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode.HALF_UP

@Singleton
class CalculationService @Inject()(connector: CurrencyConversionConnector)(implicit ec: ExecutionContext) {

  def calculate(calculationRequests: Seq[CalculationRequest])(implicit hc: HeaderCarrier): Future[CalculationResults] =
    calculationRequests.headOption.map(_.goods) match {
      case Some(importGoods: ImportGoods) =>
        importsResults(calculationRequests).map { results =>
          val threshold = calculateThresholdImport(results, calculationRequests.headOption.map(_.destination))
          CalculationResults(results, threshold)
        }

      case Some(exportGoods: ExportGoods) =>
        Future(
          CalculationResults(
            Seq.empty,
            calculateThresholdExport(calculationRequests.map(_.goods), calculationRequests.headOption.map(_.destination))))
      case None => Future(CalculationResults(Seq.empty, WithinThreshold))
    }

  private def importsResults(calculationRequests: Seq[CalculationRequest])(implicit hc: HeaderCarrier): Future[Seq[CalculationResult]] =
    Future.traverse(calculationRequests) { req =>
      calculateImports(req.goods.asInstanceOf[ImportGoods]) //TODO casting safe here but still horrible!!!
    }

  def calculateImports(importGoods: ImportGoods, date: LocalDate = LocalDate.now())(implicit hc: HeaderCarrier): Future[CalculationResult] =
    importGoods.purchaseDetails.currency.valueForConversion
      .fold(Future(calculation(importGoods, BigDecimal(1), None)))(code => findRateAndCalculate(importGoods, code, date))

  //TODO to be enhanced to handle exports too!
  def calculateThresholdImport(calculationResults: Seq[CalculationResult], destination: Option[GoodsDestination]): ThresholdCheck =
    if (calculationResults.map(_.gbpAmount.value).sum > destination.getOrElse(GreatBritain).threshold.value) OverThreshold
    else WithinThreshold

  def calculateThresholdExport(goods: Seq[Goods], destination: Option[GoodsDestination]): ThresholdCheck =
    if (goods.map(_.purchaseDetails.numericAmount).sum > destination.map(_.threshold.inPounds).getOrElse(0))
      OverThreshold
    else WithinThreshold

  private def findRateAndCalculate(importGoods: ImportGoods, code: String, date: LocalDate)(
    implicit hc: HeaderCarrier): Future[CalculationResult] =
    connector
      .getConversionRate(code, date)
      .map(
        _.find(_.currencyCode == code)
          .fold(calculation(importGoods, BigDecimal(0), None))(conversionRate =>
            calculation(importGoods, conversionRate.rate, Some(conversionRate))))

  private def calculation(
    importGoods: ImportGoods,
    rate: BigDecimal,
    conversionRatePeriod: Option[ConversionRatePeriod]): CalculationResult = {
    import importGoods._
    val converted: BigDecimal = (BigDecimal(purchaseDetails.amount) / rate).setScale(2, HALF_UP) //TODO handle possible failure
    val duty = calculateDuty(producedInEu, converted)
    val vatRate = BigDecimal(goodsVatRate.value / 100.0)
    val vat = ((converted + duty) * vatRate).setScale(2, HALF_UP)

    CalculationResult(
      importGoods,
      AmountInPence((converted * 100).toLong),
      AmountInPence((duty * 100).toLong),
      AmountInPence((vat * 100).toLong),
      conversionRatePeriod
    )
  }

  private def calculateDuty(producedInEu: YesNoDontKnow, converted: BigDecimal): BigDecimal =
    producedInEu match {
      case YesNoDontKnow.Yes => BigDecimal(0.0)
      case _                 => (converted * 0.033).setScale(2, HALF_UP)
    }
}
