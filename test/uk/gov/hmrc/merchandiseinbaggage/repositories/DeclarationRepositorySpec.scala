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

import java.time.LocalDateTime

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Seconds, Span}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, SessionId}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationId
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.mongo.MongoConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationRepositorySpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures with BeforeAndAfterEach {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))
  val reactiveMongo = new ReactiveMongoComponent { override def mongoConnector: MongoConnector = MongoConnector(mongoConf.uri)}
  val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)

  "insert a declaration object into MongoDB" in {
    val declaration = aDeclaration

    whenReady(repository.insert(declaration)) { result =>
      result mustBe declaration
    }
  }

  "find a declaration by declaration id" in {
    val declarationOne = aDeclaration
    val declarationTwo = declarationOne.copy(declarationId = DeclarationId("something different"))

    def insertTwo(): Future[Declaration] = repository.insert(declarationOne).flatMap(_ => repository.insert(declarationTwo))

    whenReady(insertTwo()) { insertResult =>
      insertResult mustBe declarationTwo
    }

    whenReady(repository.findByDeclarationId(declarationOne.declarationId)) { findResult =>
      findResult mustBe Some(declarationOne)
    }
  }

  "delete all declarations for testing purpose" in {
    val declarationOne = aDeclaration
    val declarationTwo = declarationOne.copy(declarationId = DeclarationId("something different"))

    def insertTwo(): Future[Declaration] = repository.insert(declarationOne).flatMap(_ => repository.insert(declarationTwo))

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

  "find latest for session id in a multi declaration scenario" in {
    val declaration = aDeclaration
    val declarationWithDifferentSessionId = aDeclaration.copy(sessionId = SessionId("different"))
    val dateTime = LocalDateTime.of(2020, 1, 1, 1, 1)
    val declarations: List[Declaration] = (1 to 5).toList.map(idx =>
      declaration.copy(declarationId = DeclarationId(idx.toString)).copy(dateOfDeclaration = dateTime.plusDays(idx)))

    repository.insert(declarationWithDifferentSessionId).futureValue
    declarations.foreach { dec => repository.insert(dec).futureValue }

    whenReady(repository.findAll) { res => res.size mustBe declarations.size + 1 }

    whenReady(repository.findLatestBySessionId(declaration.sessionId)) { found =>
      found.dateOfDeclaration mustBe dateTime.plusDays(5)
    }
  }

  "find latest of a list declaration created date" in {
    val declaration = aDeclaration
    val newest = 20
    val now = LocalDateTime.now
    val declarations: List[Declaration] = (1 to newest).toList.map(idx =>
      declaration.copy(declarationId = DeclarationId(idx.toString)).copy(dateOfDeclaration = now.plusMinutes(idx))
    )

    repository.latest(declarations).dateOfDeclaration.withSecond(0) mustBe now.plusMinutes(newest).withSecond(0)
  }

  override def beforeEach(): Unit = repository.deleteAll()
  override def afterEach(): Unit = repository.deleteAll()
}
