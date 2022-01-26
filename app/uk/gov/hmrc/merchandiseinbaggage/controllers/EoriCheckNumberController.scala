/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggage.connectors.EoriCheckConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.Eori
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class EoriCheckNumberController @Inject()(mcc: MessagesControllerComponents, connector: EoriCheckConnector)(
  implicit val ec: ExecutionContext)
    extends BackendController(mcc) {

  def checkEoriNumber(eoriNumber: String): Action[AnyContent] = Action.async { implicit request =>
    connector
      .checkEori(Eori(eoriNumber))
      .map { responseList =>
        responseList
          .find(_.eori == eoriNumber)
          .fold(NotFound(s"eori: $eoriNumber was not found in response")) { response =>
            Ok(Json.toJson(response))
          }
      }
      .recover {
        case _ => NotFound
      }
  }
}
