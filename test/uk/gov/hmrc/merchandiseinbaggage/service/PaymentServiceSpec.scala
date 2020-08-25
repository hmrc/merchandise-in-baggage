/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.model.Declaration
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.{CoreTestData, SpecBaseControllerSpecs}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PaymentServiceSpec extends SpecBaseControllerSpecs with CoreTestData with ScalaFutures {

  "persist a declaration" in new PaymentService {
    val persist: Declaration => Future[Boolean] = _ => Future.successful(true)

    whenReady(persistDeclaration(persist, aDeclaration)) { result =>
      result mustBe aDeclaration
    }
  }

  val mongoCRUD = injector.instanceOf[DeclarationRepository]
  "Should be able to insert person Object into MongoDB" in {
    println(mongoCRUD.insert(aDeclaration))
//    whenReady(mongoCRUD.findById(aDeclaration.id)) { res =>
//      res mustBe Some(aDeclaration)
//    }
    whenReady(mongoCRUD.findAll) { res =>
      res.isEmpty mustBe false
    }
  }
}
