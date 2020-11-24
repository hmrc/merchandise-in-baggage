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
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationServiceSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {
  private val testAuditConnector: TestAuditConnector = TestAuditConnector(Future.successful(Success), injector)

  "persist a declaration from a declaration request" in new DeclarationService {
    override val auditConnector: AuditConnector = testAuditConnector

    val declarationRequest: DeclarationRequest = aDeclarationRequest
    val declaration: Declaration = declarationRequest.toDeclaration.copy(declarationId = aDeclarationId)
    val persist: Declaration => Future[Declaration] = _ => Future.successful(declaration)

    testAuditConnector.audited.isDefined mustBe false

    whenReady(persistDeclaration(persist, declarationRequest)) { result =>
      result mustBe declaration
      testAuditConnector.audited.isDefined mustBe true
    }
  }

  "find a declaration by id or returns not found" in new DeclarationService {
    override val auditConnector: TestAuditConnector = testAuditConnector

    val declaration: Declaration = aDeclaration
    val stubbedFind: DeclarationId => Future[Option[Declaration]] = _ => Future.successful(Some(declaration))
    val stubbedNotFound: DeclarationId => Future[Option[Declaration]] = _ => Future.successful(None)

    whenReady(findByDeclarationId(stubbedFind, declaration.declarationId).value) { result =>
      result mustBe Right(declaration)
    }

    whenReady(findByDeclarationId(stubbedNotFound, declaration.declarationId).value) { result =>
      result mustBe Left(DeclarationNotFound)
    }
  }
}
