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

package uk.gov.hmrc.merchandiseinbaggage.service

import cats.data.EitherT
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, reset, when}
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, MibReference}
import uk.gov.hmrc.merchandiseinbaggage.model.core.*
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.util.Utils.FutureOps
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success

import scala.concurrent.Future

class DeclarationServiceSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {
  trait Fixture {
    val declarationRepo: DeclarationRepository = mock(classOf[DeclarationRepository])
    val emailService: EmailService             = mock(classOf[EmailService])
    val auditConnector: AuditConnector         = mock(classOf[AuditConnector])

    val declarationService = new DeclarationService(declarationRepo, emailService, auditConnector, messagesApi)

    def mockEmails(declaration: Declaration) =
      when(emailService.sendEmails(any(), any())(any())).thenReturn(EitherT.rightT[Future, BusinessError](declaration))

    def mockAudit() =
      when(
        auditConnector
          .sendExtendedEvent(any())(any(), any())
      )
        .thenReturn(Success.asFuture)

    def mockDeclarationInsert(declaration: Declaration) =
      when(declarationRepo.insertDeclaration(any())).thenReturn(Future.successful(declaration))

    def mockFindBy(
      declaration: Option[Declaration],
      mibReference: MibReference,
      amendmentReference: Option[Int] = None
    ) =
      when(declarationRepo.findBy(ArgumentMatchers.eq(mibReference), ArgumentMatchers.eq(amendmentReference)))
        .thenReturn(Future.successful(declaration))

  }

  "persistDeclaration" should {
    "persist an Export and trigger email and audit" in new Fixture {
      val declaration = aDeclaration.copy(declarationType = Export)

      mockDeclarationInsert(declaration)
      mockEmails(declaration)
      mockAudit()

      declarationService.persistDeclaration(declaration).futureValue mustBe declaration
    }

    "persist an Import and NOT trigger Audit and Emails for non zero payments" in new Fixture {
      val declaration = aDeclaration

      mockDeclarationInsert(declaration)
      declarationService.persistDeclaration(declaration).futureValue mustBe declaration
    }

    "persist an Import and trigger Audit and Emails for zero payments" in new Fixture {
      val declaration =
        aDeclaration.copy(declarationType = Import, maybeTotalCalculationResult = Some(zeroTotalCalculationResult))

      mockDeclarationInsert(declaration)
      mockEmails(declaration)
      mockAudit()
      declarationService.persistDeclaration(declaration).futureValue mustBe declaration
    }
  }

  "AmendDeclaration" should {
    "persist an Export and trigger email and audit" in new Fixture {
      val declaration = aDeclarationWithAmendment.copy(declarationType = Export)

      when(declarationRepo.upsertDeclaration(any())).thenReturn(Future.successful(declaration))
      mockEmails(declaration)

      declarationService.amendDeclaration(declaration).futureValue mustBe declaration
    }

    "NOT trigger Audit and Emails for non zero amendment payments" in new Fixture {
      val declaration = aDeclarationWithAmendment

      when(declarationRepo.upsertDeclaration(any())).thenReturn(Future.successful(declaration))

      declarationService.amendDeclaration(declaration).futureValue mustBe declaration
    }

    "trigger Audit and Emails for zero payments" in new Fixture {
      val declaration = aDeclarationWithAmendment.copy(amendments = Seq(aAmendmentWithNoTax))

      when(declarationRepo.upsertDeclaration(any())).thenReturn(Future.successful(declaration))
      mockEmails(declaration)

      declarationService.amendDeclaration(declaration).futureValue mustBe declaration
    }
  }

  "find a declaration by id or returns not found" in new Fixture {
    val declaration: Declaration = aDeclaration

    when(declarationRepo.findByDeclarationId(any())).thenReturn(Future.successful(Some(declaration)))

    declarationService.findByDeclarationId(declaration.declarationId).value.futureValue mustBe Right(declaration)

    reset(declarationRepo)
    when(declarationRepo.findByDeclarationId(any())).thenReturn(Future.successful(None))

    declarationService.findByDeclarationId(declaration.declarationId).value.futureValue mustBe Left(DeclarationNotFound)
  }

  "find a declaration by mibReference or returns not found" in new Fixture {
    val declaration: Declaration = aDeclaration
    mockFindBy(Some(declaration), declaration.mibReference, None)
    declarationService.findBy(declaration.mibReference).value.futureValue mustBe Right(declaration)

    mockFindBy(None, declaration.mibReference, None)
    declarationService.findBy(declaration.mibReference).value.futureValue mustBe Left(DeclarationNotFound)
  }

  "processPaymentCallback" should {

    "triggers emails, update paymentSuccess flag and triggers Audit for new declarations" in new Fixture {
      val declaration = aDeclaration

      mockFindBy(Some(declaration), declaration.mibReference, None)
      when(declarationRepo.upsertDeclaration(any())).thenReturn(Future.successful(declaration))
      mockEmails(declaration)
      mockAudit()

      declarationService
        .processPaymentCallback(PaymentCallbackRequest(declaration.mibReference.value, None))
        .value
        .futureValue mustBe Right(declaration)
    }

    "triggers emails, update paymentSuccess flag and triggers Audit for amend declarations" in new Fixture {
      val declaration = aDeclarationWithAmendment

      mockFindBy(Some(declaration), declaration.mibReference, Some(1))
      when(declarationRepo.upsertDeclaration(any())).thenReturn(Future.successful(declaration))
      mockEmails(declaration)
      mockAudit()

      declarationService
        .processPaymentCallback(PaymentCallbackRequest(declaration.mibReference.value, Some(1)))
        .value
        .futureValue mustBe Right(declaration)
    }
  }
}
