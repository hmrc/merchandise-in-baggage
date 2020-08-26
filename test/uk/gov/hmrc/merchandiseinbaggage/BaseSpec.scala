/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage

import de.bwaldvogel.mongo.MongoServer
import de.bwaldvogel.mongo.backend.h2.H2Backend
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.Injector
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig


trait BaseSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach

trait BaseSpecWithApplication extends BaseSpec with GuiceOneAppPerSuite {

  def injector: Injector = app.injector
  implicit val appConf: AppConfig = new AppConfig
}

trait BaseSpecWithMongoTestServer extends BaseSpec {

  private val server: MongoServer = new MongoServer(new H2Backend("database.mv"));

  override def beforeEach(): Unit = server.bind("localhost", 27017)

  override def afterEach(): Unit = server.shutdown()
}
