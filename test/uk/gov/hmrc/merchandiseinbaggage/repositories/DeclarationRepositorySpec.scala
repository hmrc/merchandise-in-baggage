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

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Seconds, Span}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Declaration, Outstanding}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.mongo.MongoConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationRepositorySpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  "insert a declaration object into MongoDB" in {
    val reactiveMongo = new ReactiveMongoComponent { override def mongoConnector: MongoConnector = MongoConnector(mongoConf.uri)}
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)
    val declaration = aDeclaration

    whenReady(repository.insert(declaration)) { result =>
      result mustBe declaration
    }
  }

  "find a declaration by declaration id" in {
    val reactiveMongo = new ReactiveMongoComponent { override def mongoConnector: MongoConnector = MongoConnector(mongoConf.uri)}
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)
    val declaration = aDeclaration

    def insertTwo(): Future[Declaration] = repository.insert(aDeclaration).flatMap(_ => repository.insert(declaration))

    whenReady(insertTwo()) { insertResult =>
      insertResult mustBe declaration
    }

    whenReady(repository.findByDeclarationId(declaration.declarationId)) { findResult =>
      findResult mustBe Some(declaration)
    }
  }

  "updates the payment status for a given declaration id" in {
    val reactiveMongo = new ReactiveMongoComponent { override def mongoConnector: MongoConnector = MongoConnector(mongoConf.uri)}
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)
    val declaration = aDeclaration
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
    val reactiveMongo = new ReactiveMongoComponent { override def mongoConnector: MongoConnector = MongoConnector(mongoConf.uri)}
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)
    val declaration = aDeclaration

    def insertTwo(): Future[Declaration] = repository.insert(aDeclaration).flatMap(_ => repository.insert(declaration))

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
}
