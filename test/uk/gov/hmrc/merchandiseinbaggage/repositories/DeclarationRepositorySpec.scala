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

package uk.gov.hmrc.merchandiseinbaggage.repositories

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Seconds, Span}
import play.api.test.Helpers.*
import uk.gov.hmrc.merchandiseinbaggage.model.api.*
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.Future

class DeclarationRepositorySpec
    extends BaseSpecWithApplication
    with CoreTestData
    with ScalaFutures
    with BeforeAndAfterEach {

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  "insert a declaration object into MongoDB" in {
    val declaration = aDeclaration

    whenReady(repository.insertDeclaration(declaration)) { result =>
      result mustBe declaration
    }
  }

  "inserting a duplicate declaration should not trigger an error" in {
    val declaration = aDeclaration

    whenReady(repository.insertDeclaration(declaration)) { result =>
      result mustBe declaration
    }

    whenReady(repository.insertDeclaration(declaration)) { result =>
      result mustBe declaration
    }
  }

  "upsert a declaration object into MongoDB" in {
    val declaration = aDeclaration

    whenReady(repository.upsertDeclaration(declaration)) { result =>
      result mustBe declaration
    }
  }

  "find a declaration by declaration id" in {
    val declarationOne = aDeclaration
    val declarationTwo = declarationOne.copy(declarationId = DeclarationId("something different"))

    def insertTwo(): Future[Declaration] =
      repository.insertDeclaration(declarationOne).flatMap(_ => repository.insertDeclaration(declarationTwo))

    whenReady(insertTwo()) { insertResult =>
      insertResult mustBe declarationTwo
    }

    whenReady(repository.findByDeclarationId(declarationOne.declarationId)) { findResult =>
      findResult mustBe Some(declarationOne)
    }
  }

  "find a declaration by mibReference" in {
    val declaration = aDeclaration

    def insert(): Future[Declaration] = repository.insertDeclaration(declaration)

    whenReady(insert()) { insertResult =>
      insertResult mustBe declaration
    }

    whenReady(repository.findBy(declaration.mibReference)) { findResult =>
      findResult mustBe Some(declaration)
    }
  }

  "find a declaration by mibReference & Eori" in {
    val declarationOne = aDeclaration
    val declarationTwo = declarationOne
      .copy(
        declarationId = DeclarationId("something different"),
        mibReference = MibReference("another-mib"),
        eori = Eori("another-eori")
      )

    def insertTwo(): Future[List[Declaration]] =
      for {
        _   <- repository.insertDeclaration(declarationOne)
        _   <- repository.insertDeclaration(declarationTwo)
        all <- repository.findAll
      } yield all

    whenReady(insertTwo()) { result =>
      result mustBe List(declarationOne, declarationTwo)
    }

    whenReady(repository.findBy(declarationOne.mibReference, declarationOne.eori)) { findResult =>
      findResult mustBe Some(declarationOne)
    }

    whenReady(repository.findBy(declarationTwo.mibReference, declarationTwo.eori)) { findResult =>
      findResult mustBe Some(declarationTwo)
    }

    // invalid combinations of (mibRef, eori)
    whenReady(repository.findBy(declarationOne.mibReference, declarationTwo.eori)) { findResult =>
      findResult mustBe None
    }
  }

  "find a declaration by mibReference & Amendment Reference" in {
    val declaration = aDeclarationWithAmendment
    // insert first
    whenReady(repository.insertDeclaration(declaration)) { result =>
      result mustBe declaration
    }

    whenReady(repository.findBy(declaration.mibReference, declaration.amendments.headOption.map(_.reference))) {
      findResult =>
        findResult mustBe Some(declaration)
    }
  }

  "delete all declarations for testing purpose" in {
    val declarationOne = aDeclaration
    val declarationTwo = declarationOne.copy(declarationId = DeclarationId("something different"))

    def insertTwo(): Future[Declaration] =
      repository.insertDeclaration(declarationOne).flatMap(_ => repository.insertDeclaration(declarationTwo))

    val collection = for {
      _   <- repository.deleteAll()
      _   <- insertTwo()
      _   <- repository.deleteAll()
      all <- repository.findAll
    } yield all

    whenReady(collection) { deleteResult =>
      deleteResult mustBe Nil
    }
  }

  override def beforeEach(): Unit = await(repository.deleteAll())

  override def afterEach(): Unit = await(repository.deleteAll())
}
