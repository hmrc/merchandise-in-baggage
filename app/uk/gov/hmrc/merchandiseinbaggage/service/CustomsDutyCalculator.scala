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

package uk.gov.hmrc.merchandiseinbaggage.service

import java.time.LocalDate

import cats.data.EitherT
import cats.instances.future._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.{CalculationRequest, CurrencyConversionResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Amount, BusinessError, CurrencyNotFound}
import uk.gov.hmrc.merchandiseinbaggage.repositories.CustomsRate._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait CustomsDutyCalculator extends CurrencyConversionConnector {

  def customDuty(httpClient: HttpClient, calculationRequest: CalculationRequest)
                (implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, BusinessError, Amount] =
    for {
      rates      <- EitherT.liftF(findCurrencyRate(httpClient, calculationRequest.currency, LocalDate.now))
      customDuty <- EitherT.fromEither(calculateConvertedRate(rates, calculationRequest))
    } yield customDuty


  private def calculateConvertedRate(currencyRates: List[CurrencyConversionResponse],
                                     calculationRequest: CalculationRequest): Either[BusinessError, Amount] =
    (for {
      code        <- currencyRates.find(_.currencyCode == calculationRequest.currency)
      rate        <- code.rate
      calculation <- Try(Amount((calculationRequest.amount.value / rate.toDouble) * customFlatRate)).toOption
    } yield calculation).fold(Left(CurrencyNotFound): Either[BusinessError, Amount])(res => Right(res))
}
