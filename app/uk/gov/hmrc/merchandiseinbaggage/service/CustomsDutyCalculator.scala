/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import java.time.LocalDate

import cats.data.EitherT
import cats.instances.future._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.{CalculationRequest, CurrencyConversionResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.core.AmountInPence._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{AmountInPence, BusinessError, CurrencyNotFound}
import uk.gov.hmrc.merchandiseinbaggage.repositories.CustomsRate._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait CustomsDutyCalculator extends CurrencyConversionConnector {

  def customDuty(httpClient: HttpClient, calculationRequest: CalculationRequest)
                (implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, BusinessError, AmountInPence] =
    for {
      lt         <- EitherT.liftF(findCurrencyRate(httpClient, calculationRequest.currency, LocalDate.now))
      customDuty <- EitherT.fromEither(calculateConvertedRate(lt, calculationRequest))
    } yield customDuty


  private def calculateConvertedRate(currencyRates: List[CurrencyConversionResponse],
                                     calculationRequest: CalculationRequest): Either[BusinessError, AmountInPence] =
    (for {
      code        <- currencyRates.find(_.currencyCode == calculationRequest.currency)
      rate        <- code.rate
      calculation <- Try(((calculationRequest.amount.value / rate.toDouble) * customFlatRate).twoDecimalsHalfUp).toOption
    } yield calculation).fold(Left(CurrencyNotFound): Either[BusinessError, AmountInPence])(res => Right(res))
}
