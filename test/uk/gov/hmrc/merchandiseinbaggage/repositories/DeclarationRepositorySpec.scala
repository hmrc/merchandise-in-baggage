/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.repositories

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, BaseSpecWithMongoTestServer, CoreTestData}

class DeclarationRepositorySpec extends BaseSpecWithApplication with BaseSpecWithMongoTestServer with CoreTestData with ScalaFutures {

  "insert a declaration object into MongoDB" in {
    val repository = injector.instanceOf[DeclarationRepository]
    val declaration = aDeclaration

    whenReady(repository.insert(declaration)) { res =>
      res mustBe declaration
    }
  }
}
