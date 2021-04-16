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
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation._
import uk.gov.hmrc.merchandiseinbaggage.model.api.{AmountInPence, ConversionRatePeriod, GoodsDestination, YesNoDontKnow}

import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode.HALF_UP

@Singleton
class CalculationService @Inject()(connector: CurrencyConversionConnector)(implicit ec: ExecutionContext) {

  def calculate(calculationRequest: CalculationRequest, date: LocalDate = LocalDate.now())(
    implicit hc: HeaderCarrier): Future[CalculationResult] =
    calculationRequest.goods.purchaseDetails.currency.valueForConversion
      .fold(Future(calculation(calculationRequest, BigDecimal(1), None)))(code => findRateAndCalculate(calculationRequest, code, date))

  //TODO to be enhanced to handle exports too!
  def calculateThreshold(calculationResults: Seq[CalculationResult], destination: Option[GoodsDestination]): ThresholdCheck =
    if (calculationResults.map(_.gbpAmount.value).sum > destination.getOrElse(GreatBritain).threshold.value) OverThreshold
    else WithinThreshold

  private def findRateAndCalculate(calculationRequest: CalculationRequest, code: String, date: LocalDate)(
    implicit hc: HeaderCarrier): Future[CalculationResult] =
    connector
      .getConversionRate(code, date)
      .map(
        _.find(_.currencyCode == code)
          .fold(calculation(calculationRequest, BigDecimal(0), None))(conversionRate =>
            calculation(calculationRequest, conversionRate.rate, Some(conversionRate))))

  private def calculation(
    calculationRequest: CalculationRequest,
    rate: BigDecimal,
    conversionRatePeriod: Option[ConversionRatePeriod]): CalculationResult = {
    import calculationRequest.goods._
    val converted: BigDecimal = (BigDecimal(purchaseDetails.amount) / rate).setScale(2, HALF_UP) //TODO handle possible failure
    val duty = calculateDuty(producedInEu, converted)
    val vatRate = BigDecimal(goodsVatRate.value / 100.0)
    val vat = ((converted + duty) * vatRate).setScale(2, HALF_UP)

    CalculationResult(
      calculationRequest.goods,
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
