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

package uk.gov.hmrc.merchandiseinbaggage.repositories.crypto

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Seconds, Span}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepositoryImpl
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.mongo.MongoConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationRepositoryCryptoImplSpec
    extends BaseSpecWithApplication with CoreTestData with ScalaFutures with BeforeAndAfterEach with MongoConfiguration {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))
  private val reactiveMongo = new ReactiveMongoComponent { override def mongoConnector: MongoConnector = MongoConnector(mongoConf.uri) }
  lazy val rep = new DeclarationRepositoryImpl(reactiveMongo.mongoConnector.db)
  private val declarationCrypto = injector.instanceOf[DeclarationCrypto]

  val cryptoRepository: DeclarationRepositoryCryptoImpl = new DeclarationRepositoryCryptoImpl(rep, declarationCrypto)

  "insert a declaration object into MongoDB" in {
    val declaration = aDeclaration

    whenReady(cryptoRepository.insertDeclaration(declaration)) { result =>
      result mustBe declaration
    }
  }

  "inserting a duplicate declaration should not trigger an error" in {
    val declaration = aDeclaration

    whenReady(cryptoRepository.insertDeclaration(declaration)) { result =>
      result mustBe declaration
    }

    whenReady(cryptoRepository.insertDeclaration(declaration)) { result =>
      result mustBe declaration
    }
  }

  "find a declaration by declaration id" in {
    val declarationOne = aDeclaration
    val declarationTwo = declarationOne.copy(declarationId = DeclarationId("something different"))

    def insertTwo(): Future[Declaration] =
      cryptoRepository.insertDeclaration(declarationOne).flatMap(_ => cryptoRepository.insertDeclaration(declarationTwo))

    whenReady(insertTwo()) { insertResult =>
      insertResult mustBe declarationTwo
    }

    whenReady(cryptoRepository.findByDeclarationId(declarationOne.declarationId)) { findResult =>
      findResult mustBe Some(declarationOne)
    }
  }

  "find a declaration by mibReference" in {
    val declaration = aDeclaration

    def insert(): Future[Declaration] = cryptoRepository.insertDeclaration(declaration)

    whenReady(insert()) { insertResult =>
      insertResult mustBe declaration
    }

    whenReady(cryptoRepository.findBy(declaration.mibReference)) { findResult =>
      findResult mustBe Some(declaration)
    }
  }

  "find a declaration by mibReference & Eori" in {
    val declarationOne = aDeclaration
    val declarationTwo = declarationOne
      .copy(declarationId = DeclarationId("something different"), mibReference = MibReference("another-mib"), eori = Eori("another-eori"))

    def insertTwo(): Future[List[Declaration]] =
      for {
        _   <- cryptoRepository.insertDeclaration(declarationOne)
        _   <- cryptoRepository.insertDeclaration(declarationTwo)
        all <- cryptoRepository.findAll
      } yield all

    whenReady(insertTwo()) { result =>
      result mustBe List(declarationOne, declarationTwo)
    }

    whenReady(cryptoRepository.findBy(declarationOne.mibReference, declarationOne.eori)) { findResult =>
      findResult mustBe Some(declarationOne)
    }

    whenReady(cryptoRepository.findBy(declarationTwo.mibReference, declarationTwo.eori)) { findResult =>
      findResult mustBe Some(declarationTwo)
    }

    //invalid combinations of (mibRef, eori)
    whenReady(cryptoRepository.findBy(declarationOne.mibReference, declarationTwo.eori)) { findResult =>
      findResult mustBe None
    }
  }

  "delete all declarations for testing purpose" in {
    val declarationOne = aDeclaration
    val declarationTwo = declarationOne.copy(declarationId = DeclarationId("something different"))

    def insertTwo(): Future[Declaration] =
      cryptoRepository.insertDeclaration(declarationOne).flatMap(_ => cryptoRepository.insertDeclaration(declarationTwo))

    val collection = for {
      _   <- cryptoRepository.deleteAll()
      _   <- insertTwo()
      _   <- cryptoRepository.deleteAll()
      all <- cryptoRepository.findAll
    } yield all

    whenReady(collection) { deleteResult =>
      deleteResult mustBe Nil
    }
  }

  "return the latest for session id in a multi declaration scenario" in {
    val declaration = aDeclaration
    val declarationTwo = aDeclaration
    val declarationWithDifferentSessionId = aDeclaration.copy(sessionId = SessionId("different"))
    val repository = new DeclarationRepositoryImpl(reactiveMongo.mongoConnector.db) {
      override def latest(declarations: List[Declaration]): Declaration = declarationTwo
    }

    def insertThree(): Future[Declaration] =
      for {
        _     <- repository.insertDeclaration(declaration)
        _     <- repository.insertDeclaration(declarationTwo)
        three <- repository.insertDeclaration(declarationWithDifferentSessionId)
      } yield three

    insertThree().futureValue mustBe declarationWithDifferentSessionId
    repository.findLatestBySessionId(declaration.sessionId).futureValue mustBe declarationTwo
  }

  "test upgrade mongodb to encrypted" in {
    val repository = new DeclarationRepositoryImpl(reactiveMongo.mongoConnector.db)
    val declaration = aDeclaration

    // insert unencrypted dec into mongo

    List
      .fill(100)(declaration)
      .zipWithIndex
      .map { d =>
        d._1.copy(declarationId = DeclarationId(s"idx-${d._2}"), mibReference = MibReference(s"idx-${d._2}"))
      }
      .foreach { d =>
        whenReady(repository.insertDeclaration(d)) { e =>
          e
        }
      }

    whenReady(repository.insertDeclaration(declaration)) { insertResult =>
      insertResult mustBe declaration
    }

    // upgrade
    whenReady(cryptoRepository.upgradeCollectionToEncrypted()) { result =>
      result mustBe true
    }

    // retrieve using encryped and check
    whenReady(cryptoRepository.findBy(declaration.mibReference)) { findResult =>
      findResult mustBe Some(declaration)
    }
  }

  override def beforeEach(): Unit = cryptoRepository.deleteAll()
  override def afterEach(): Unit = cryptoRepository.deleteAll()
}
