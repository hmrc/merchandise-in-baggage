/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.Injector
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.config.MongoConfiguration
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository

trait BaseSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach with BeforeAndAfterAll

trait BaseSpecWithApplication extends BaseSpec with GuiceOneServerPerSuite with MongoConfiguration with ScalaFutures {
  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(500L, Milliseconds)))

  def injector: Injector = app.injector

  lazy val repository: DeclarationRepository = injector.instanceOf[DeclarationRepository]

  def buildGet(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
}
