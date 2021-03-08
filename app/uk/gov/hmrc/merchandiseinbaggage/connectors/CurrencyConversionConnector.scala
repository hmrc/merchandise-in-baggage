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

package uk.gov.hmrc.merchandiseinbaggage.connectors

import java.time.LocalDate.now
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.model.api.ConversionRatePeriod

import javax.inject.{Inject, Named, Singleton}
import uk.gov.hmrc.merchandiseinbaggage.config.CurrencyConversionConfiguration

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CurrencyConversionConnector @Inject()(
  httpClient: HttpClient,
  @Named("currencyConversionBaseUrl") baseUrl: String
) extends CurrencyConversionConfiguration {

  def getConversionRate(code: String, date: LocalDate = LocalDate.now())(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[Seq[ConversionRatePeriod]] =
    httpClient.GET[Seq[ConversionRatePeriod]](s"$baseUrl${currencyConversionConf.currencyConversionUrl}$date?cc=$code")
}
