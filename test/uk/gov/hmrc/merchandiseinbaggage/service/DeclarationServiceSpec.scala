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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationServiceSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {

  "persist a declaration from a declaration request" in new DeclarationService {
    val declarationRequest: DeclarationRequest = aDeclarationRequest
    val declaration: Declaration = declarationRequest.toDeclaration.copy(declarationId = aDeclarationId)
    val persist: Declaration => Future[Declaration] = _ => Future.successful(declaration)

    whenReady(persistDeclaration(persist, declarationRequest)) { result =>
      result mustBe declaration
    }
  }

  "find a declaration by id or returns not found" in new DeclarationService {
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
