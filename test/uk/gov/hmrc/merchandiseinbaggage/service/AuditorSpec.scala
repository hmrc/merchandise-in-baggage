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

import org.scalatest.concurrent.ScalaFutures
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditorSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {
  private val failed = Failure("failed")

  "auditDeclaration" should {
    Seq(Success, Disabled, failed).foreach { auditStatus =>
      s"delegate to the auditConnector and return $auditStatus" in new Auditor {
        private val declaration = aDeclaration

        override val auditConnector: TestAuditConnector = TestAuditConnector(Future.successful(auditStatus), injector)
        override val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

        auditDeclaration(declaration).futureValue mustBe (())

        private val auditedEvent = auditConnector.audited.get
        auditedEvent.auditSource mustBe "merchandise-in-baggage"
        auditedEvent.auditType mustBe "DeclarationComplete"
        auditedEvent.detail mustBe toJson(declaration)

        (auditedEvent.detail \ "source").as[String] mustBe "Digital"
      }
    }

    "use DeclarationAmended event for amendments" in new Auditor {
      private val declaration = aDeclarationWithAmendment

      override val auditConnector: TestAuditConnector = TestAuditConnector(Future.successful(Success), injector)
      override val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      auditDeclaration(declaration).futureValue mustBe (())

      private val auditedEvent = auditConnector.audited.get

      auditedEvent.auditType mustBe "DeclarationAmended"
    }

    "handle auditConnector failure" in new Auditor {
      override val auditConnector: TestAuditConnector =
        TestAuditConnector(Future.failed(new RuntimeException("failed")), injector)

      override val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      auditDeclaration(aDeclaration).futureValue mustBe (())
    }
  }

  "RefundableDeclaration" should {
    s"trigger refund events for new declarations" in new Auditor {
      private val declaration = aDeclaration

      override val auditConnector: TestAuditConnector = TestAuditConnector(Future.successful(Success), injector)
      override val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      auditRefundableDeclaration(declaration).futureValue mustBe (())

      private val auditedEvent = auditConnector.audited.get

      auditedEvent.auditSource mustBe "merchandise-in-baggage"
      auditedEvent.auditType mustBe "RefundableDeclaration"

      val refundJson =
        """{"mibReference":"mib-ref-1234","name":"Terry Crews","eori":"eori-test","goodsCategory":"test","gbpValue":"£1.00","customsDuty":"£1.00","vat":"£1.00","vatRate":"5%","paymentAmount":"£2.00","producedInEu":"Yes","purchaseAmount":"100","currencyCode":"GBP","exchangeRate":"1.00"}"""

      auditedEvent.detail mustBe Json.parse(refundJson)
    }

    s"trigger refund events for amend declarations" in new Auditor {
      private val declaration = aDeclarationWithAmendment

      override val auditConnector: TestAuditConnector = TestAuditConnector(Future.successful(Success), injector)
      override val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      auditRefundableDeclaration(declaration, Some(aAmendment)).futureValue mustBe (())

      private val auditedEvent = auditConnector.audited.get

      auditedEvent.auditSource mustBe "merchandise-in-baggage"
      auditedEvent.auditType mustBe "RefundableDeclaration"

      val refundJson =
        """{"mibReference":"mib-ref-1234","name":"Terry Crews","eori":"eori-test","goodsCategory":"test","gbpValue":"£1.00","customsDuty":"£1.00","vat":"£1.00","vatRate":"5%","paymentAmount":"£2.00","producedInEu":"Yes","purchaseAmount":"100","currencyCode":"GBP","exchangeRate":"1.00"}"""

      auditedEvent.detail mustBe Json.parse(refundJson)
    }
  }
}
