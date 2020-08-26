/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.repositories

import org.scalatest.concurrent.ScalaFutures
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, BaseSpecWithMongoTestServer, CoreTestData}

class DeclarationRepositorySpec extends BaseSpecWithApplication with BaseSpecWithMongoTestServer with CoreTestData with ScalaFutures {

  "insert a declaration object into MongoDB" in {
    val reactiveMongo = injector.instanceOf[ReactiveMongoComponent]
    val repository = new DeclarationRepository(reactiveMongo.mongoConnector.db)
    val declaration = aDeclaration

    whenReady(repository.insert(declaration)) { res =>
      res mustBe declaration
    }
  }
}
