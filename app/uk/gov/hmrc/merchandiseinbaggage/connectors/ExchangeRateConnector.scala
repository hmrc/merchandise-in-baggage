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

import org.jsoup.Jsoup
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.merchandiseinbaggage.config.ExchangeRateConfiguration

@Singleton
class ExchangeRateConnector @Inject()(httpClient: HttpClient) extends ExchangeRateConfiguration {

  private val serverUrl = "https://assets.publishing.service.gov.uk"

  def monthlyURL(year: Int = LocalDate.now.getYear)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {
    val yearlyUrl: String =
      s"${exchangeRateConf.exchangeRateUrl}/government/publications/hmrc-exchange-rates-for-$year-monthly"

    httpClient
      .GET[HttpResponse](yearlyUrl)
      .map { response =>
        serverUrl + findFirstLink(response.body)
      }
      .recoverWith {
        case _ => Future.successful(yearlyUrl)
      }
  }

  private def findFirstLink(body: String): String =
    Jsoup
      .parse(body)
      .select("h2.gem-c-heading")
      .nextAll
      .select("a")
      .first()
      .attributes()
      .get("href")
}
