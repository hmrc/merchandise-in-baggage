/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.merchandiseinbaggage.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig

class MicroserviceHelloWorldControllerSpec extends BaseSpecWithApplication {

  private val fakeRequest = FakeRequest("GET", "/")
  private val appConfig     = new AppConfig()

  private val controller = new MicroserviceHelloWorldController(appConfig, Helpers.stubControllerComponents())

  "GET /" should {
    "return 200" in {
      val result = controller.hello()(fakeRequest)
      status(result) mustBe Status.OK
    }
  }
}
