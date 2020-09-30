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

package uk.gov.hmrc.merchandiseinbaggage.connectors

import java.time.LocalDate

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson
import uk.gov.hmrc.merchandiseinbaggage.config.CurrencyConversionConfiguration
import uk.gov.hmrc.merchandiseinbaggage.factories.ServiceFactory
import uk.gov.hmrc.merchandiseinbaggage.model.api.CurrencyConversionResponse

import scala.concurrent.{ExecutionContext, Future}

trait CurrencyConversionConnector extends CurrencyConversionConfiguration with ServiceFactory {

  def findCurrencyRate(currencyCode: String, date: LocalDate)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[CurrencyConversionResponse]] =
    httpClient.GET[List[CurrencyConversionResponse]](s"${currencyConversionUrl(date, currencyCode)}")
}
