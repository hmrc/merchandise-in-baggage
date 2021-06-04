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

package uk.gov.hmrc.merchandiseinbaggage.service

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import play.api.inject.{ApplicationLifecycle, DefaultApplicationLifecycle, Injector}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditChannel, AuditConnector, AuditCounter, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.{ExecutionContext, Future}

trait TestAuditConnector extends AuditConnector {
  def audited: Option[ExtendedDataEvent]
}

object TestAuditConnector {
  def apply(result: Future[AuditResult], injector: Injector): TestAuditConnector = new TestAuditConnector {
    override val auditingConfig: AuditingConfig = injector.instanceOf[AuditingConfig]

    private var auditedEvent: Option[ExtendedDataEvent] = None

    override def audited: Option[ExtendedDataEvent] = auditedEvent

    override def sendExtendedEvent(event: ExtendedDataEvent)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
      auditedEvent = Some(event)
      result
    }

    override def auditChannel: AuditChannel = new AuditChannel {
      override def auditingConfig: AuditingConfig = AuditingConfig(
        consumer = None,
        enabled = false,
        auditSource = "myApp",
        auditSentHeaders = false
      )

      override def materializer: Materializer = ActorMaterializer()(ActorSystem())

      override def lifecycle: ApplicationLifecycle = new DefaultApplicationLifecycle()
    }

    override def auditCounter: AuditCounter = new AuditCounter {
      override def createMetadata(): JsObject = Json.obj()
    }
  }
}
