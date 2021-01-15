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

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.{AmountInPence, CalculationResult, Country, Currency}
import uk.gov.hmrc.merchandiseinbaggage.model.calculation.CalculationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.currencyconversion.ConversionRatePeriod

import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode.HALF_UP

@Singleton
class CalculationService @Inject()(connector: CurrencyConversionConnector)(implicit ec: ExecutionContext) {

  def calculate(calculationRequest: CalculationRequest)(implicit hc: HeaderCarrier): Future[CalculationResult] = {
    import calculationRequest._

    val futureRate = currency.valueForConversion.fold(Future.successful(BigDecimal(1))) { code =>
      connector.getConversionRate(code).map(_.find(_.currencyCode == code).fold(BigDecimal(0))(_.rate))
    }

    futureRate.map { rate =>
      calculation(amount, country, calculationRequest, rate)
    }
  }

  private def calculation(amount: BigDecimal, country: Country, calculationRequest: CalculationRequest, rate: BigDecimal) = {
    val converted: BigDecimal = (amount / rate).setScale(2, HALF_UP)

    val duty = calculateDuty(country, converted)
    val vatRate = BigDecimal(calculationRequest.vatRate.value / 100.0)
    val vat = ((converted + duty) * vatRate).setScale(2, HALF_UP)

    CalculationResult(
      AmountInPence((converted * 100).toLong),
      AmountInPence((duty * 100).toLong),
      AmountInPence((vat * 100).toLong)
    )
  }

  def findRate(currency: Currency)(findConversionRate: String => Future[Seq[ConversionRatePeriod]])
              (implicit hc: HeaderCarrier): Future[BigDecimal] =
    currency.valueForConversion.fold(Future.successful(BigDecimal(1))) { code =>
      findConversionRate(code).map(_.find(_.currencyCode == code).fold(BigDecimal(0))(_.rate))
  }

  private def calculateDuty(country: Country, converted: BigDecimal): BigDecimal =
    if (country.isEu) BigDecimal(0.0)
    else (converted * 0.033).setScale(2, HALF_UP)
}
