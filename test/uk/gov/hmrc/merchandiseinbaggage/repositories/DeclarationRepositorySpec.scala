/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.repositories

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Milliseconds, Seconds, Span}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, BaseSpecWithMongoTestServer, CoreTestData}

class DeclarationRepositorySpec extends BaseSpecWithApplication with BaseSpecWithMongoTestServer with CoreTestData with ScalaFutures {

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  "insert a declaration object into MongoDB" in {
    val reactiveMongo = injector.instanceOf[ReactiveMongoComponent]
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)
    val declaration = aDeclaration

    whenReady(repository.insert(declaration)) { result =>
      result mustBe declaration
    }
  }
}
