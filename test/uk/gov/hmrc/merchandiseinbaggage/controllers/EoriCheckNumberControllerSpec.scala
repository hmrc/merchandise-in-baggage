/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.merchandiseinbaggage.connectors.EoriCheckConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.Eori
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse

import scala.concurrent.{ExecutionContext, Future}

class EoriCheckNumberControllerSpec extends BaseSpecWithApplication with CoreTestData {

  private val client = injector.instanceOf[HttpClientV2]

  "handle a EORI check request by making a call to check number API" in {
    val connector      = new EoriCheckConnector(appConfig, client) {
      override def checkEori(eori: Eori)(implicit
        hc: HeaderCarrier,
        ec: ExecutionContext
      ): Future[List[CheckResponse]] =
        Future.successful(aCheckResponse :: Nil)
    }
    val controller     = new EoriCheckNumberController(component, connector)
    val number         = "GB025115110987654"
    val request        = buildGet(routes.EoriCheckNumberController.checkEoriNumber(number).url)
    val eventualResult = controller.checkEoriNumber(number)(request)

    status(eventualResult) mustBe 200
    contentAsJson(eventualResult) mustBe Json.toJson(aCheckResponse)
  }

  "handle EORI not existing in CheckResponse" in {
    val connector = new EoriCheckConnector(appConfig, client) {
      override def checkEori(eori: Eori)(implicit
        hc: HeaderCarrier,
        ec: ExecutionContext
      ): Future[List[CheckResponse]] =
        Future.successful(aCheckResponse.copy(eori = "GB123") :: Nil)
    }

    val controller     = new EoriCheckNumberController(component, connector)
    val number         = "GB025115110987654"
    val request        = buildGet(routes.EoriCheckNumberController.checkEoriNumber(number).url)
    val eventualResult = controller.checkEoriNumber(number)(request)

    status(eventualResult) mustBe 404
  }

  "handle a EORI check request call failure" in {
    val connector      = new EoriCheckConnector(appConfig, client) {
      override def checkEori(eori: Eori)(implicit
        hc: HeaderCarrier,
        ec: ExecutionContext
      ): Future[List[CheckResponse]] =
        Future.failed(new Exception("unable to connect"))
    }
    val controller     = new EoriCheckNumberController(component, connector)
    val number         = "GB025115110987654"
    val request        = buildGet(routes.EoriCheckNumberController.checkEoriNumber(number).url)
    val eventualResult = controller.checkEoriNumber(number)(request)

    status(eventualResult) mustBe 404
  }
}
