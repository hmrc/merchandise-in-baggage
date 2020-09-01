/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.repositories

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Seconds, Span}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Declaration, Outstanding, Paid}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationRepositorySpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  "insert a declaration object into MongoDB" in {
    val reactiveMongo = injector.instanceOf[ReactiveMongoComponent]
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)
    val declaration = aDeclaration

    whenReady(repository.insert(declaration)) { result =>
      result mustBe declaration
    }
  }

  "find a declaration by declaration id" in {
    val reactiveMongo = injector.instanceOf[ReactiveMongoComponent]
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)
    val declaration = aDeclaration

    def insertTwo(): Future[Declaration] = repository.insert(aDeclaration).flatMap(_ => repository.insert(declaration))

    whenReady(insertTwo()) { insertResult =>
      insertResult mustBe declaration
    }

    whenReady(repository.findByDeclarationId(declaration.declarationId)) { findResult =>
      findResult mustBe declaration :: Nil
    }
  }

  "updates the payment status for a given declaration id" in {
    val reactiveMongo = injector.instanceOf[ReactiveMongoComponent]
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)
    val declaration = aDeclaration
    val updatedStatus = Paid

    whenReady(repository.insert(declaration)) { insertResult =>
      insertResult mustBe declaration
      declaration.paymentStatus mustBe Outstanding
    }

    whenReady(repository.updateStatus(declaration, updatedStatus)) { patchResult =>
      patchResult mustBe true
    }

    whenReady(repository.findByDeclarationId(declaration.declarationId)) { findResult =>
      findResult.head mustBe declaration.copy(paymentStatus = updatedStatus)
    }
  }

  "delete all declarations for testing purpose" in {
    val reactiveMongo = injector.instanceOf[ReactiveMongoComponent]
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)
    val declaration = aDeclaration

    def insertTwo(): Future[Declaration] = repository.insert(aDeclaration).flatMap(_ => repository.insert(declaration))

    //TODO assert inserted first
    val collection = for {
      _ <- repository.deleteAll
      _ <- insertTwo()
      _ <- repository.deleteAll
      all <- repository.findAll
    } yield all

    whenReady(collection) { deleteResult =>
      deleteResult mustBe Nil
    }
  }
}
