/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.json.Json

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.model.DeclarationEmailInfo

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

@Singleton
class EmailConnector @Inject() (appConfig: AppConfig, http: HttpClientV2) {

  def sendEmails(
    emailInformation: DeclarationEmailInfo
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Int] =
    http
      .post(url"${appConfig.emailUrl}/transactionengine/email")
      .withBody(Json.toJson(emailInformation))
      .execute[HttpResponse]
      .map(_.status)

}
