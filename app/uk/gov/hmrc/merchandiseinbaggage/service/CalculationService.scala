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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationRequest, CalculationResult}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{AmountInPence, ConversionRatePeriod, YesNoDontKnow}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode.HALF_UP

@Singleton
class CalculationService @Inject()(connector: CurrencyConversionConnector)(implicit ec: ExecutionContext) {

  def calculate(calculationRequest: CalculationRequest)(implicit hc: HeaderCarrier): Future[CalculationResult] =
    calculationRequest.currency.valueForConversion
      .fold(Future(calculation(calculationRequest, BigDecimal(1), None)))(code => findRateAndCalculate(calculationRequest, code))

  private def findRateAndCalculate(calculationRequest: CalculationRequest, code: String)(
    implicit hc: HeaderCarrier): Future[CalculationResult] =
    connector
      .getConversionRate(code)
      .map(
        _.find(_.currencyCode == code)
          .fold(calculation(calculationRequest, BigDecimal(0), None))(conversionRate =>
            calculation(calculationRequest, conversionRate.rate, Some(conversionRate))))

  private def calculation(
    calculationRequest: CalculationRequest,
    rate: BigDecimal,
    conversionRatePeriod: Option[ConversionRatePeriod]): CalculationResult = {
    import calculationRequest._
    val converted: BigDecimal = (amount / rate).setScale(2, HALF_UP)
    val duty = calculateDuty(producedInEu, converted)
    val vatRate = BigDecimal(calculationRequest.vatRate.value / 100.0)
    val vat = ((converted + duty) * vatRate).setScale(2, HALF_UP)

    CalculationResult(
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
