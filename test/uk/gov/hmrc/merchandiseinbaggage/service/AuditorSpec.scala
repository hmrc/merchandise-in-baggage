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

import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditorSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {
  private val failed = Failure("failed")

  "auditDeclarationComplete" should {
    Seq(Success, Disabled, failed).foreach { auditStatus =>
      s"delegate to the auditConnector and return $auditStatus" in new Auditor {
        private val declaration = aDeclaration

        override val auditConnector: TestAuditConnector = TestAuditConnector(Future.successful(auditStatus), injector)

        auditDeclarationComplete(declaration).futureValue mustBe auditStatus

        private val auditedEvent = auditConnector.audited.get
        auditedEvent.auditSource mustBe "merchandise-in-baggage"
        auditedEvent.auditType mustBe "DeclarationComplete"
        auditedEvent.detail mustBe toJson(declaration)
      }
    }

    "handle auditConnector failure" in new Auditor {
      override val auditConnector: TestAuditConnector =
        TestAuditConnector(Future.failed(new RuntimeException("failed")), injector)

      auditDeclarationComplete(aDeclaration).futureValue mustBe failed
    }
  }
}
