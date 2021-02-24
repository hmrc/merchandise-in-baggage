/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.merchandiseinbaggage

import akka.stream.Materializer
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Second, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, MessagesControllerComponents}
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepositoryImpl

trait BaseSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach with BeforeAndAfterAll with Eventually

trait BaseSpecWithApplication extends BaseSpec with GuiceOneServerPerSuite with ScalaFutures {
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(scaled(Span(5L, Seconds)), scaled(Span(1L, Second)))

  lazy val testServerPort = port

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  implicit val mat: Materializer = app.materializer
  implicit val appConfig: AppConfig = injector.instanceOf[AppConfig]
  lazy val component = injector.instanceOf[MessagesControllerComponents]
  lazy val repository = injector.instanceOf[DeclarationRepositoryImpl]

  implicit val messagesApi = app.injector.instanceOf[MessagesApi]
  lazy val injector: Injector = app.injector

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(Map(
        "play.http.router"                               -> "testOnlyDoNotUseInAppConf.Routes",
        "microservice.services.currency-conversion.port" -> WireMock.port,
        "microservice.services.email.port"               -> WireMock.port,
        "microservice.services.eori-check.port"          -> WireMock.port
      ))
      .build()

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("", "").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  def buildPost(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(POST, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def buildGet(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, url).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
}
