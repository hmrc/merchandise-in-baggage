/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import java.time.LocalDate

import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.{CalculationRequest, CurrencyConversionResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.core.AmountInPence
import uk.gov.hmrc.merchandiseinbaggage.repositories.CustomsRate._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait CustomsDutyCalculator extends CurrencyConversionConnector {

  def customDuty(httpClient: HttpClient, calculationRequest: CalculationRequest)
                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AmountInPence] =
    for {
      lt         <- findCurrencyConversion(httpClient, calculationRequest.currency, LocalDate.now)
      customDuty <- Future.fromTry(calculateConvertedRate(lt, calculationRequest))
    } yield customDuty


  private def calculateConvertedRate(currencyRates: List[CurrencyConversionResponse], calculationRequest: CalculationRequest): Try[AmountInPence] = Try {
    val rate = currencyRates.find(_.currencyCode == calculationRequest.currency).get.rate.toDouble
    AmountInPence(BigDecimal((calculationRequest.amount.value / rate) * customFlatRate)
      .setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
  }


}
