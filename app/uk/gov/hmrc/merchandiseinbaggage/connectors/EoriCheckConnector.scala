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

import javax.inject.{Inject, Named, Singleton}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggage.config.EoriCheckConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.Eori
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EoriCheckConnector @Inject()(httpClient: HttpClient, @Named("eoriCheckBaseUrl") base: String) extends EoriCheckConfiguration {

  def checkEori(eori: Eori)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CheckResponse] =
    httpClient.GET[CheckResponse](s"$base${eoriCheckConf.eoriCheckUrl}${eori.toString}")
}
