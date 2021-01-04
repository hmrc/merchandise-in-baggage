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
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, MibReference}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class DeclarationServiceSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures with MockFactory {
  private val testAuditConnector: TestAuditConnector = TestAuditConnector(Future.successful(Success), injector)
  val declarationRepo = mock[DeclarationRepository]
  val emailService = mock[EmailService]

  val declarationService = new DeclarationService(declarationRepo, emailService, testAuditConnector)

  "persist a declaration from a Export declaration request and trigger Audit" in {
    val declaration = aDeclaration.copy(declarationType = Export)

    (declarationRepo.insertDeclaration(_: Declaration)).expects(*).returns(Future.successful(declaration))

    testAuditConnector.audited.isDefined mustBe false

    declarationService.persistDeclaration(declaration).futureValue mustBe declaration
    testAuditConnector.audited.isDefined mustBe true
  }

  "persist a declaration from a Import declaration request and do NOT trigger Audit" in {
    val testAuditConnector: TestAuditConnector = TestAuditConnector(Future.successful(Success), injector)
    val declaration = aDeclaration

    (declarationRepo.insertDeclaration(_: Declaration)).expects(*).returns(Future.successful(declaration))

    declarationService.persistDeclaration(declaration).futureValue mustBe declaration
    testAuditConnector.audited.isDefined mustBe false
  }

  "find a declaration by id or returns not found" in {
    val declaration: Declaration = aDeclaration

    (declarationRepo.findByDeclarationId(_: DeclarationId)).expects(declaration.declarationId).returns(Future.successful(Some(declaration)))
    declarationService.findByDeclarationId(declaration.declarationId).value.futureValue mustBe Right(declaration)

    (declarationRepo.findByDeclarationId(_: DeclarationId)).expects(declaration.declarationId).returns(Future.successful(None))
    declarationService.findByDeclarationId(declaration.declarationId).value.futureValue mustBe Left(DeclarationNotFound)
  }

  "find a declaration by mibReference or returns not found" in {
    val declaration: Declaration = aDeclaration

    (declarationRepo.findByMibReference(_: MibReference)).expects(declaration.mibReference).returns(Future.successful(Some(declaration)))
    declarationService.findByMibReference(declaration.mibReference).value.futureValue mustBe Right(declaration)

    (declarationRepo.findByMibReference(_: MibReference)).expects(declaration.mibReference).returns(Future.successful(None))
    declarationService.findByMibReference(declaration.mibReference).value.futureValue mustBe Left(DeclarationNotFound)
  }

  "sendEmails must return result as expected" in {
    val declaration: Declaration = aDeclaration
    (declarationRepo.findByDeclarationId(_: DeclarationId)).expects(declaration.declarationId).returns(Future.successful(Some(declaration)))
    (emailService
      .sendEmails(_: Declaration)(_: HeaderCarrier))
      .expects(*, *)
      .returns(EitherT[Future, BusinessError, Unit](Future.successful(Right(()))))
    Await.result(declarationService.sendEmails(declaration.declarationId).value, 5.seconds) mustBe Right(())
  }

  "processPaymentCallback triggers email delivery, update paymentSuccess flag and trigger Audit" in {
    val testAuditConnector: TestAuditConnector = TestAuditConnector(Future.successful(Success), injector)
    val declarationService = new DeclarationService(declarationRepo, emailService, testAuditConnector)
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
