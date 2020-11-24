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

package uk.gov.hmrc.merchandiseinbaggage.service

import play.api.libs.json.Json.toJson
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Failure
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait Auditor {
  val auditConnector: AuditConnector

  def auditDeclarationPersisted(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] =
    auditConnector.sendExtendedEvent(
      ExtendedDataEvent(
        auditSource = "merchandise-in-baggage",
        auditType = "declarationPersisted",
        detail = toJson(declaration))).recover {
      case NonFatal(e) => Failure(e.getMessage)
    }
}
