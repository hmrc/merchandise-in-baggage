/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.Injector
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig


trait BaseSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach

trait SpecBaseControllerSpecs extends BaseSpec with GuiceOneAppPerSuite with CoreTestData {

  def injector: Injector = app.injector

  implicit val appConf: AppConfig = new AppConfig

}
