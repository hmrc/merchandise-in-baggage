/*
 * Copyright 2024 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.model.api.Eori
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EoriCheckConnector @Inject() (appConfig: AppConfig, httpClient: HttpClient) {

  def checkEori(eori: Eori)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[CheckResponse]] =
    httpClient.GET[List[CheckResponse]](s"${appConfig.eoriCheckUrl}/check-eori-number/check-eori/${eori.toString}")
}
