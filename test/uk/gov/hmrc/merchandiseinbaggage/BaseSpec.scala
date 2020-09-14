/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.Injector
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration


trait BaseSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach with BeforeAndAfterAll

trait BaseSpecWithApplication extends BaseSpec with GuiceOneAppPerSuite with MongoConfiguration {
  def injector: Injector = app.injector

  def buildPost(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(POST, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def buildGet(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
}
