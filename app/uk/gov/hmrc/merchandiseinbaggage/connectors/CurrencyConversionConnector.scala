/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.connectors

import java.time.LocalDate

import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.config.CurrencyConversionConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.CurrencyConversionResponse

import scala.concurrent.{ExecutionContext, Future}

trait CurrencyConversionConnector extends CurrencyConversionConfiguration {

  def findCurrencyConversion(httpClient: HttpClient, currencyCode: String, date: LocalDate)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[CurrencyConversionResponse]] =
    httpClient.GET[List[CurrencyConversionResponse]](s"${currencyConversionUrl(date, currencyCode)}")

}
