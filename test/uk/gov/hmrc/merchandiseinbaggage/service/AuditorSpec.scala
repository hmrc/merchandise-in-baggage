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

package uk.gov.hmrc.merchandiseinbaggage.service

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import play.api.i18n.MessagesApi
import play.api.libs.json.Json.{parse, toJson}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.{ExecutionContext, Future}

class AuditorSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures with MockFactory {
  private val failed = Failure("failed")

  private val mockAuditConnector = mock[AuditConnector]

  private val auditor = new Auditor {
    override val auditConnector: AuditConnector = mockAuditConnector
    override val messagesApi: MessagesApi       = app.injector.instanceOf[MessagesApi]
  }

  "auditDeclaration" should {
    Seq(Success, Disabled, failed).foreach { auditStatus =>
      s"delegate to the auditConnector and return $auditStatus" in {
        val declaration = aDeclaration

        (mockAuditConnector
          .sendExtendedEvent(_: ExtendedDataEvent)(_: HeaderCarrier, _: ExecutionContext))
          .expects(where { (auditedEvent: ExtendedDataEvent, _: HeaderCarrier, _: ExecutionContext) =>
            auditedEvent.auditSource == "merchandise-in-baggage" &&
            auditedEvent.auditType == "DeclarationComplete" &&
            auditedEvent.detail == toJson(declaration) &&
            (auditedEvent.detail \ "source").as[String] == "Digital"
          })
          .returning(Future.successful(auditStatus))

        auditor.auditDeclaration(declaration).futureValue mustBe (())

      }
    }

    "use DeclarationAmended event for amendments" in {
      val declaration = aDeclarationWithAmendment
      (mockAuditConnector
        .sendExtendedEvent(_: ExtendedDataEvent)(_: HeaderCarrier, _: ExecutionContext))
        .expects(where { (auditedEvent: ExtendedDataEvent, _: HeaderCarrier, _: ExecutionContext) =>
          auditedEvent.auditType == "DeclarationAmended"
        })
        .returning(Future.successful(AuditResult.Success))

      auditor.auditDeclaration(declaration).futureValue mustBe (())
    }
  }

  "RefundableDeclaration" should {
    s"trigger refund events for new declarations" in {
      val declaration = aDeclaration
      val refundJson  =
        """{"mibReference":"mib-ref-1234","name":"Terry Crews","eori":"eori-test","goodsCategory":"test","gbpValue":"£1.00","customsDuty":"£1.00","vat":"£1.00","vatRate":"5%","paymentAmount":"£2.00","producedInEu":"Yes","purchaseAmount":"100","currencyCode":"GBP","exchangeRate":"1.00"}"""

      (mockAuditConnector
        .sendExtendedEvent(_: ExtendedDataEvent)(_: HeaderCarrier, _: ExecutionContext))
        .expects(where { (auditedEvent: ExtendedDataEvent, _: HeaderCarrier, _: ExecutionContext) =>
          auditedEvent.auditType == "RefundableDeclaration" &&
          auditedEvent.detail == parse(refundJson)
        })
        .returning(Future.successful(AuditResult.Success))

      auditor.auditRefundableDeclaration(declaration).futureValue mustBe (())

    }

    s"trigger refund events for amend declarations" in {
      val declaration = aDeclarationWithAmendment
      val refundJson  =
        """{"mibReference":"mib-ref-1234","name":"Terry Crews","eori":"eori-test","goodsCategory":"test","gbpValue":"£1.00","customsDuty":"£1.00","vat":"£1.00","vatRate":"5%","paymentAmount":"£2.00","producedInEu":"Yes","purchaseAmount":"100","currencyCode":"GBP","exchangeRate":"1.00"}"""

      (mockAuditConnector
        .sendExtendedEvent(_: ExtendedDataEvent)(_: HeaderCarrier, _: ExecutionContext))
        .expects(where { (auditedEvent: ExtendedDataEvent, _: HeaderCarrier, _: ExecutionContext) =>
          auditedEvent.auditType == "RefundableDeclaration" &&
          auditedEvent.detail == parse(refundJson)
        })
        .returning(Future.successful(AuditResult.Success))

      auditor.auditRefundableDeclaration(declaration).futureValue mustBe (())
    }
  }
}
