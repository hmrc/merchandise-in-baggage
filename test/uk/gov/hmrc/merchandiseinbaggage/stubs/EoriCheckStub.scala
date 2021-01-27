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

package uk.gov.hmrc.merchandiseinbaggage.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlPathEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.config.EoriCheckConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.Eori

object EoriCheckStub extends EoriCheckConfiguration with CoreTestData {

  def givenEoriCheck(eori: Eori)(implicit server: WireMockServer): StubMapping =
    server.stubFor(
      get(urlPathEqualTo(s"${eoriCheckConf.eoriCheckUrl}${eori.toString}"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(aSuccessCheckResponse)))
}
