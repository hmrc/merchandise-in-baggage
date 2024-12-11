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
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.time.{Milliseconds, Seconds, Span}
import play.api.test.Helpers.*
import uk.gov.hmrc.merchandiseinbaggage.model.api.*
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.{Address, AddressLookupCountry}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.Future

class CryptoDeclarationRepositoryImplSpec
    extends BaseSpecWithApplication
    with CoreTestData
    with ScalaFutures
    with BeforeAndAfterEach {

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(1000L, Milliseconds)))

  "with database we " should {
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
      val declarationOne = aDeclaration.copy(encrypted = Some(true))
      val declarationTwo = declarationOne.copy(declarationId = DeclarationId("something different"))

      def insertTwo(): Future[Declaration] =
        cryptoRepository
          .insertDeclaration(declarationOne)
          .flatMap(_ => cryptoRepository.insertDeclaration(declarationTwo))

      whenReady(insertTwo()) { insertResult =>
        insertResult mustBe declarationTwo
      }

      whenReady(cryptoRepository.findByDeclarationId(declarationOne.declarationId)) { findResult =>
        findResult mustBe Some(declarationOne)
      }
    }

    "find a declaration by mibReference" in {
      val declaration = aDeclaration.copy(encrypted = Some(true))

      def insert(): Future[Declaration] = cryptoRepository.insertDeclaration(declaration)

      whenReady(insert()) { insertResult =>
        insertResult mustBe declaration
      }

      whenReady(cryptoRepository.findBy(declaration.mibReference)) { findResult =>
        findResult mustBe Some(declaration)
      }
    }

    "find a declaration by mibReference & Eori" in {
      val declarationOne = aDeclaration.copy(encrypted = Some(true))
      val declarationTwo = declarationOne
        .copy(
          declarationId = DeclarationId("something different"),
          mibReference = MibReference("another-mib"),
          eori = Eori("another-eori")
        )

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

      // invalid combinations of (mibRef, eori)
      whenReady(cryptoRepository.findBy(declarationOne.mibReference, declarationTwo.eori)) { findResult =>
        findResult mustBe None
      }
    }

    "delete all declarations for testing purpose" in {
      val declarationOne = aDeclaration
      val declarationTwo = declarationOne.copy(declarationId = DeclarationId("something different"))

      def insertTwo(): Future[Declaration] =
        cryptoRepository
          .insertDeclaration(declarationOne)
          .flatMap(_ => cryptoRepository.insertDeclaration(declarationTwo))

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

    "Update pre-encrypted declaration, simulate adding Amendment" in {
      val declaration = aDeclaration

      // Write a non-encrypted declaration
      repository.insertDeclaration(declaration).futureValue.encrypted mustBe None

      // Find declaration (ie start of amendment)
      val declarationAfterFindBy = whenReady(cryptoRepository.findBy(declaration.mibReference)) { findResult =>
        findResult mustBe Some(declaration)
        findResult.map(_.encrypted mustBe None)
        findResult.getOrElse(fail())
      }

      // Add Amendment and update declaration
      val declarationAfterFindByWithAmendment = declarationAfterFindBy.copy(amendments = Seq(aAmendment))
      whenReady(cryptoRepository.upsertDeclaration(declarationAfterFindByWithAmendment)) { findResult =>
        findResult.encrypted mustBe None
      }

      // Find and check it is now encrypted
      whenReady(cryptoRepository.findBy(declarationAfterFindBy.mibReference)) { findResult =>
        findResult.map { declarationWithEncryption =>
          declarationWithEncryption.encrypted mustBe Some(true)
          declarationWithEncryption.nameOfPersonCarryingTheGoods.firstName mustBe declaration.nameOfPersonCarryingTheGoods.firstName
          declarationWithEncryption.nameOfPersonCarryingTheGoods.lastName mustBe declaration.nameOfPersonCarryingTheGoods.lastName
          declarationWithEncryption.email mustBe declaration.email
          declarationWithEncryption.eori mustBe declaration.eori
        }
      }
    }
  }

  "crypto " should {
    val declarationCrypto = injector.instanceOf[CryptoDeclarationRepositoryImpl]
    val declaration       = aDeclaration

    "encrypt -> decrypt must match the original with encrypted enabled" in {
      declarationCrypto.decryptDeclaration(declarationCrypto.encryptDeclaration(declaration)) mustBe declaration.copy(
        encrypted = Some(true)
      )
    }

    "encrypt name should be different from original" in {
      declarationCrypto.encryptDeclaration(declaration).nameOfPersonCarryingTheGoods mustBe Name(
        "5DAVpR9ozhA60/y+q9gHIA==",
        "QoaA6uK44jJz9bOa1MFeSg=="
      )
    }

    "encrypt email should be different from original" in {
      declarationCrypto.encryptDeclaration(declaration).email mustBe Some(Email("WNHUI7IOAftY2n4/WZ2MPQ=="))
    }

    "encrypt eori should be different from original" in {
      declarationCrypto.encryptDeclaration(declaration).eori mustBe Eori("LPanFBawrTuKZY5axJt3zA==")
    }

    "encrypt maybeCustomsAgent should be different from original" in {
      val declarationWithCustomsAgent = declaration.copy(maybeCustomsAgent = Some(aCustomsAgent))

      declarationCrypto
        .encryptDeclaration(declarationWithCustomsAgent)
        .maybeCustomsAgent mustBe Some(
        CustomsAgent(
          "ZjgQ7zTCvwpY/U+Kc7SnkA==",
          Address(
            List("rAIsO/sEpJi2Bo0w+cmYFw==", "+v5ElPX1X/ZRBtWQy9pWTw=="),
            Some("xj3SFcsYP02+4ljJXml2Bw=="),
            AddressLookupCountry("GB", Some("UK"))
          )
        )
      )
    }

    "encrypt journeyDetails should be different from original" in {
      val declarationWithJourneyDetails = declaration.copy(journeyDetails = aJourneyInASmallVehicle)

      declarationCrypto
        .encryptDeclaration(declarationWithJourneyDetails)
        .journeyDetails mustBe JourneyInSmallVehicle(
        Port("DVR", "title.dover", true, List("Port of Dover")),
        aJourneyInASmallVehicle.dateOfTravel,
        "N/x/Y3gF3SqaY/RzJ4oIfQ=="
      )
    }

  }

  override def beforeEach(): Unit = await(repository.deleteAll())

  override def afterEach(): Unit = await(repository.deleteAll())
}
