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

package uk.gov.hmrc.merchandiseinbaggage.repositories

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Seconds, Span}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationBE, DeclarationId, Outstanding}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.mongo.MongoConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationBERepositorySpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures with BeforeAndAfterEach {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  val reactiveMongo = new ReactiveMongoComponent { override def mongoConnector: MongoConnector = MongoConnector(mongoConf.uri)}
  val repository = new DeclarationBERepository(reactiveMongo.mongoConnector.db)

  "insert a declaration object into MongoDB" in {
    val declaration = aDeclarationBE

    whenReady(repository.insert(declaration)) { result =>
      result mustBe declaration
    }
  }

  "find a declaration by declaration id" in {
    val declaration = aDeclarationBE
    val declarationTwo = aDeclarationBE.copy(declarationId = DeclarationId("different"))

    def insertTwo(): Future[DeclarationBE] = repository.insert(declaration).flatMap(_ => repository.insert(declarationTwo))

    whenReady(insertTwo()) { insertResult =>
      insertResult mustBe declarationTwo
    }

    whenReady(repository.findByDeclarationId(declarationTwo.declarationId)) { findResult =>
      findResult mustBe Some(declarationTwo)
    }
  }

  "updates the payment status for a given declaration id" in {
    val declaration = aDeclarationBE
    val updatedDeclaration = declaration.withPaidStatus

    whenReady(repository.insert(declaration)) { insertResult =>
      insertResult mustBe declaration
      declaration.paymentStatus mustBe Outstanding
    }

    whenReady(repository.updateStatus(declaration, updatedDeclaration.paymentStatus)) { patchResult =>
      patchResult mustBe updatedDeclaration
    }

    whenReady(repository.findByDeclarationId(declaration.declarationId)) { findResult =>
      findResult mustBe Some(updatedDeclaration)
    }
  }

  "delete all declarations for testing purpose" in {
    val declaration = aDeclarationBE
    val declarationTwo = declaration.copy(declarationId = DeclarationId("something different"))

    def insertTwo(): Future[DeclarationBE] = repository.insert(declaration).flatMap(_ => repository.insert(declarationTwo))

    val collection = for {
      _ <- repository.deleteAll()
      _ <- insertTwo()
      _ <- repository.deleteAll()
      all <- repository.findAll
    } yield all

    whenReady(collection) { deleteResult =>
      deleteResult mustBe Nil
    }
  }

  override def beforeEach(): Unit = repository.deleteAll()
  override def afterEach(): Unit = repository.deleteAll()
}
