/*
 * Copyright 2022 HM Revenue & Customs
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
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationId, MibReference}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.util.Utils.FutureOps
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DeclarationServiceSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures with MockFactory {
  trait Fixture {
    val declarationRepo: DeclarationRepository = mock[DeclarationRepository]
    val emailService: EmailService = mock[EmailService]
    val auditConnector: AuditConnector = mock[AuditConnector]

    val declarationService = new DeclarationService(declarationRepo, emailService, auditConnector, messagesApi)

    def mockEmails(declaration: Declaration) =
      (emailService
        .sendEmails(_: Declaration, _: Option[Int])(_: HeaderCarrier))
        .expects(*, *, *)
        .returns(EitherT[Future, BusinessError, Declaration](Future.successful(Right(declaration))))

    def mockAudit() =
      (auditConnector
        .sendExtendedEvent(_: ExtendedDataEvent)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *)
        .returning(Success.asFuture)
        .anyNumberOfTimes()

    def mockDeclarationInsert(declaration: Declaration) =
      (declarationRepo.insertDeclaration(_: Declaration)).expects(*).returns(Future.successful(declaration))

    def mockFindBy(declaration: Option[Declaration], mibReference: MibReference, amendmentReference: Option[Int] = None) =
      (declarationRepo
        .findBy(_: MibReference, _: Option[Int]))
        .expects(mibReference, amendmentReference)
        .returns(Future.successful(declaration))
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
      val declaration = aDeclaration.copy(declarationType = Import, maybeTotalCalculationResult = Some(zeroTotalCalculationResult))

      mockDeclarationInsert(declaration)
      mockEmails(declaration)
      mockAudit()
      declarationService.persistDeclaration(declaration).futureValue mustBe declaration
    }
  }

  "AmendDeclaration" should {
    "persist an Export and trigger email and audit" in new Fixture {
      val declaration = aDeclarationWithAmendment.copy(declarationType = Export)

      (declarationRepo.upsertDeclaration(_: Declaration)).expects(*).returns(Future.successful(declaration))
      mockEmails(declaration)

      declarationService.amendDeclaration(declaration).futureValue mustBe declaration
    }

    "NOT trigger Audit and Emails for non zero amendment payments" in new Fixture {
      val declaration = aDeclarationWithAmendment
      (declarationRepo.upsertDeclaration(_: Declaration)).expects(*).returns(Future.successful(declaration))
      declarationService.amendDeclaration(declaration).futureValue mustBe declaration
    }

    "trigger Audit and Emails for zero payments" in new Fixture {
      val declaration = aDeclarationWithAmendment.copy(amendments = Seq(aAmendmentWithNoTax))

      (declarationRepo.upsertDeclaration(_: Declaration)).expects(*).returns(Future.successful(declaration))
      mockEmails(declaration)
      declarationService.amendDeclaration(declaration).futureValue mustBe declaration
    }
  }

  "find a declaration by id or returns not found" in new Fixture {
    val declaration: Declaration = aDeclaration

    (declarationRepo.findByDeclarationId(_: DeclarationId)).expects(declaration.declarationId).returns(Future.successful(Some(declaration)))
    declarationService.findByDeclarationId(declaration.declarationId).value.futureValue mustBe Right(declaration)

    (declarationRepo.findByDeclarationId(_: DeclarationId)).expects(declaration.declarationId).returns(Future.successful(None))
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
      (declarationRepo.upsertDeclaration(_: Declaration)).expects(*).returns(Future.successful(declaration))
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
      (declarationRepo.upsertDeclaration(_: Declaration)).expects(*).returns(Future.successful(declaration))
      mockEmails(declaration)
      mockAudit()

      declarationService
        .processPaymentCallback(PaymentCallbackRequest(declaration.mibReference.value, Some(1)))
        .value
        .futureValue mustBe Right(declaration)
    }
  }
}
