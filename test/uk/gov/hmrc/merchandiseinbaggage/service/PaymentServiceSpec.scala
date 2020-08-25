/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.model.Declaration
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PaymentServiceSpec extends BaseSpec with CoreTestData with ScalaFutures {

  "persist a declaration" in new PaymentService {
    val persist: Declaration => Future[Boolean] = _ => Future.successful(true)

    whenReady(persistDeclaration(persist, aDeclaration)) { result =>
      result mustBe aDeclaration
    }
  }
}
