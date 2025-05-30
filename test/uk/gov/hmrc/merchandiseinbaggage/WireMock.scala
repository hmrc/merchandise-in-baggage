/*
 * Copyright 2025 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.WireMockServer
import org.scalatest.{BeforeAndAfterEach, Suite}

trait WireMock extends BeforeAndAfterEach { this: Suite =>

  implicit val wireMockServer: WireMockServer = new WireMockServer(WireMock.port)

  override def beforeEach(): Unit = {
    super.beforeEach()
    wireMockServer.start()
  }

  override def afterEach(): Unit =
    wireMockServer.stop()
}

object WireMock {
  val port: Int = 17777
}
