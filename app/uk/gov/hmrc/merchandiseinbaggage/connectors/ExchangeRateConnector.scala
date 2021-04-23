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
import org.jsoup.nodes.Document

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.{Success, Try}

@Singleton
class ExchangeRateConnector @Inject() extends ExchangeLinkLoaderImpl {

  def getExchangeRateUrl(year: Int = LocalDate.now.getYear): Future[String] =
    getMonthlyUrl(s"https://www.gov.uk/government/publications/hmrc-exchange-rates-for-$year-monthly")
}

trait ExchangeLinkLoader {
  def getMonthlyUrl(yearUrl: String): Future[String]
}

class ExchangeLinkLoaderImpl extends ExchangeLinkLoader {
  private val serverUrl = "https://assets.publishing.service.gov.uk"

  override def getMonthlyUrl(yearUrl: String): Future[String] =
    Try {
      val response = getPage(yearUrl)
      findFirstLink(response)
    } match {
      case Success(url) => Future.successful(serverUrl + url)
      case _            => Future.successful(yearUrl)
    }

  def getPage(yearlyUrl: String): Document = Jsoup.connect(yearlyUrl).get()

  def findFirstLink(doc: Document): String =
    doc
      .select("h2.gem-c-heading")
      .nextAll
      .select("a")
      .first()
      .attributes()
      .get("href")
}
