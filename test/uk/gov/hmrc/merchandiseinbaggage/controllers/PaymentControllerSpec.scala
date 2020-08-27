/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.merchandiseinbaggage.BaseSpecWithApplication
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.test.Helpers._

class PaymentControllerSpec extends BaseSpecWithApplication {

  private lazy val component = app.injector.instanceOf[MessagesControllerComponents]

  "on submit will trigger a call to pay-api and render the response" in {
    val controller = new PaymentController(component)

    val postRequest = buildPost(routes.PaymentController.onPayment().url)

    status(controller.onPayment()(postRequest)) mustBe 200
  }
}
