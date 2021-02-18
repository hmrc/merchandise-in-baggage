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

import cats.data.EitherT
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationId, MibReference}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class DeclarationServiceSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures with MockFactory {
  val declarationRepo: DeclarationRepository = mock[DeclarationRepository]
  val emailService: EmailService = mock[EmailService]

  private def mockEmails() =
    (emailService
      .sendEmails(_: Declaration)(_: HeaderCarrier))
      .expects(*, *)
      .returns(EitherT[Future, BusinessError, Unit](Future.successful(Right(()))))

  def declarationService(auditConnector: AuditConnector) =
    new DeclarationService(declarationRepo, emailService, auditConnector, messagesApi)

  "persist a declaration from a Export declaration request and trigger Audit & Emails" in {
    val declaration = aDeclaration.copy(declarationType = Export)

    (declarationRepo.insertDeclaration(_: Declaration)).expects(*).returns(Future.successful(declaration))
    mockEmails()

    val testAuditConnector = TestAuditConnector(Future.successful(Success), injector)
    val service = declarationService(testAuditConnector)

    testAuditConnector.audited.isDefined mustBe false

    service.persistDeclaration(declaration).futureValue mustBe declaration
    testAuditConnector.audited.isDefined mustBe true
  }

  "persist an Import declaration" should {
    "NOT trigger Audit and Emails for non zero payments" in {
      val declaration = aDeclaration

      (declarationRepo.insertDeclaration(_: Declaration)).expects(*).returns(Future.successful(declaration))

      val testAuditConnector = TestAuditConnector(Future.successful(Success), injector)
      val service = declarationService(testAuditConnector)

      testAuditConnector.audited.isDefined mustBe false

      service.persistDeclaration(declaration).futureValue mustBe declaration
      testAuditConnector.audited.isDefined mustBe false
    }

    "trigger Audit and Emails for zero payments" in {
      val declaration = aDeclaration.copy(declarationType = Import, maybeTotalCalculationResult = Some(zeroTotalCalculationResult))

      (declarationRepo.insertDeclaration(_: Declaration)).expects(*).returns(Future.successful(declaration))
      (declarationRepo.upsertDeclaration(_: Declaration)).expects(*).returns(Future.successful(declaration))
      mockEmails()

      val testAuditConnector = TestAuditConnector(Future.successful(Success), injector)
      val service = declarationService(testAuditConnector)
      testAuditConnector.audited.isDefined mustBe false

      service.persistDeclaration(declaration).futureValue mustBe declaration
      testAuditConnector.audited.isDefined mustBe true
    }
  }

  "find a declaration by id or returns not found" in {
    val declaration: Declaration = aDeclaration
    val testAuditConnector = TestAuditConnector(Future.successful(Success), injector)
    val service = declarationService(testAuditConnector)

    (declarationRepo.findByDeclarationId(_: DeclarationId)).expects(declaration.declarationId).returns(Future.successful(Some(declaration)))
    service.findByDeclarationId(declaration.declarationId).value.futureValue mustBe Right(declaration)

    (declarationRepo.findByDeclarationId(_: DeclarationId)).expects(declaration.declarationId).returns(Future.successful(None))
    service.findByDeclarationId(declaration.declarationId).value.futureValue mustBe Left(DeclarationNotFound)
  }

  "find a declaration by mibReference or returns not found" in {
    val declaration: Declaration = aDeclaration
    val testAuditConnector = TestAuditConnector(Future.successful(Success), injector)
    val service = declarationService(testAuditConnector)

    (declarationRepo.findByMibReference(_: MibReference)).expects(declaration.mibReference).returns(Future.successful(Some(declaration)))
    service.findByMibReference(declaration.mibReference).value.futureValue mustBe Right(declaration)

    (declarationRepo.findByMibReference(_: MibReference)).expects(declaration.mibReference).returns(Future.successful(None))
    service.findByMibReference(declaration.mibReference).value.futureValue mustBe Left(DeclarationNotFound)
  }

  "sendEmails must return result as expected" in {
    val declaration: Declaration = aDeclaration
    val testAuditConnector = TestAuditConnector(Future.successful(Success), injector)
    val service = declarationService(testAuditConnector)

    (declarationRepo.findByDeclarationId(_: DeclarationId)).expects(declaration.declarationId).returns(Future.successful(Some(declaration)))
    mockEmails()
    Await.result(service.sendEmails(declaration.declarationId).value, 5.seconds) mustBe Right(())
  }

  "processPaymentCallback triggers email delivery, update paymentSuccess flag and trigger Audit" in {
    val testAuditConnector: TestAuditConnector = TestAuditConnector(Future.successful(Success), injector)
    val declarationService = new DeclarationService(declarationRepo, emailService, testAuditConnector, messagesApi)
    val declaration = aDeclaration

    (declarationRepo.findByMibReference(_: MibReference)).expects(declaration.mibReference).returns(Future.successful(Some(declaration)))
    (declarationRepo.upsertDeclaration(_: Declaration)).expects(*).returns(Future.successful(declaration))
    (emailService
      .sendEmails(_: Declaration)(_: HeaderCarrier))
      .expects(*, *)
      .returns(EitherT[Future, BusinessError, Unit](Future.successful(Right(()))))

    Await.result(declarationService.processPaymentCallback(declaration.mibReference).value, 5.seconds) mustBe Right(())
    testAuditConnector.audited.isDefined mustBe true
  }
}
