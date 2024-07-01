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

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.i18n.MessagesApi
import play.api.libs.json.Json.{parse, toJson}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.Future

class AuditorSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {

  private val mockAuditConnector = mock(classOf[AuditConnector])

  private val extendedDataEvent: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

  private val auditor = new Auditor {
    override val auditConnector: AuditConnector = mockAuditConnector
    override val messagesApi: MessagesApi       = app.injector.instanceOf[MessagesApi]
  }

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(mockAuditConnector)
  }

  "auditDeclaration" should {
    Seq(Success, Disabled, Failure("failed")).foreach { auditStatus =>
      s"delegate to the auditConnector and return $auditStatus" in {
        val declaration = aDeclaration

        when(mockAuditConnector.sendExtendedEvent(extendedDataEvent.capture())(any(), any()))
          .thenReturn(Future.successful(auditStatus))

        auditor.auditDeclaration(declaration).futureValue mustBe ()

        verify(mockAuditConnector).sendExtendedEvent(extendedDataEvent.capture())(any(), any())

        val extendedDataEventValue = extendedDataEvent.getValue
        extendedDataEventValue.auditSource mustBe "merchandise-in-baggage"
        extendedDataEventValue.auditType mustBe "DeclarationComplete"
        extendedDataEventValue.detail mustBe toJson(declaration)
      }
    }

    "use DeclarationAmended event for amendments" in {
      val declaration = aDeclarationWithAmendment

      when(mockAuditConnector.sendExtendedEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

      auditor.auditDeclaration(declaration).futureValue mustBe ()

      verify(mockAuditConnector).sendExtendedEvent(extendedDataEvent.capture())(any(), any())

      val extendedDataEventValue = extendedDataEvent.getValue
      extendedDataEventValue.auditType mustBe "DeclarationAmended"
    }
  }

  "RefundableDeclaration" should {
    s"trigger refund events for new declarations" in {
      val declaration = aDeclaration
      val refundJson  =
        """{"mibReference":"mib-ref-1234","name":"Terry Crews","eori":"eori-test","goodsCategory":"test","gbpValue":"£1.00","customsDuty":"£1.00","vat":"£1.00","vatRate":"5%","paymentAmount":"£2.00","producedInEu":"Yes","purchaseAmount":"100","currencyCode":"GBP","exchangeRate":"1.00"}"""

      when(mockAuditConnector.sendExtendedEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

      auditor.auditRefundableDeclaration(declaration).futureValue mustBe ()

      verify(mockAuditConnector).sendExtendedEvent(extendedDataEvent.capture())(any(), any())

      val extendedDataEventValue = extendedDataEvent.getValue
      extendedDataEventValue.auditType mustBe "RefundableDeclaration"
      extendedDataEventValue.detail mustBe parse(refundJson)
    }

    s"trigger refund events for amend declarations" in {
      val declaration = aDeclarationWithAmendment
      val refundJson  =
        """{"mibReference":"mib-ref-1234","name":"Terry Crews","eori":"eori-test","goodsCategory":"test","gbpValue":"£1.00","customsDuty":"£1.00","vat":"£1.00","vatRate":"5%","paymentAmount":"£2.00","producedInEu":"Yes","purchaseAmount":"100","currencyCode":"GBP","exchangeRate":"1.00"}"""

      when(mockAuditConnector.sendExtendedEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

      auditor.auditRefundableDeclaration(declaration).futureValue mustBe ()

      verify(mockAuditConnector).sendExtendedEvent(extendedDataEvent.capture())(any(), any())

      val extendedDataEventValue = extendedDataEvent.getValue
      extendedDataEventValue.auditType mustBe "RefundableDeclaration"
      extendedDataEventValue.detail mustBe parse(refundJson)
    }
  }
}
