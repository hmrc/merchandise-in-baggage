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

package uk.gov.hmrc.merchandiseinbaggage.stubs

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, post, urlPathEqualTo}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.model.DeclarationEmailInfo

object EmailStub {

  def givenEmailSuccess(declarationEmailInfo: DeclarationEmailInfo)(implicit server: WireMockServer): StubMapping =
    server.stubFor(
      post(urlPathEqualTo("/transactionengine/email"))
        .withRequestBody(equalToJson(Json.toJson(declarationEmailInfo).toString()))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
        )
    )
}
