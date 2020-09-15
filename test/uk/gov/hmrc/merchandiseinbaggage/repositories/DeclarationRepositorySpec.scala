/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.repositories

import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationId
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global

class DeclarationRepositorySpec extends BaseSpecWithApplication with CoreTestData {
  private val declaration2 = aDeclaration.copy(declarationId = DeclarationId())

  override def beforeEach(): Unit = repository.deleteAll().futureValue

  private def insertTwo() = repository.insert(aDeclaration).flatMap(_ => repository.insert(declaration2))

  "insert a declaration object into MongoDB" in {
    val declaration = aDeclaration

    whenReady(repository.insert(declaration)) { result =>
      result mustBe declaration
    }
  }

  "find a declaration by declaration id" in {
    whenReady(insertTwo()) { insertResult =>
      insertResult mustBe declaration2
    }

    whenReady(repository.findByDeclarationId(aDeclaration.declarationId)) { findResult =>
      findResult mustBe Some(aDeclaration)
    }
  }

  "delete all declarations for testing purpose" in {
    val collection = for {
      _ <- insertTwo()
      _ <- repository.deleteAll()
      all <- repository.findAll
    } yield all

    whenReady(collection) { deleteResult =>
      deleteResult mustBe Nil
    }
  }
}
