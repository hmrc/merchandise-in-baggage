/*
 * Copyright 2020 HM Revenue & Customs
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

import cats.instances.future._
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.model.api.CalculationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core.CurrencyNotFound
import uk.gov.hmrc.merchandiseinbaggage.service.CustomsDutyCalculator
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class CalculatorController @Inject()(mcc: MessagesControllerComponents, httpClient: HttpClient)
                                    (implicit val ec: ExecutionContext)
  extends BackendController(mcc) with CustomsDutyCalculator {

  def onCalculations(): Action[CalculationRequest] = Action(parse.json[CalculationRequest]).async { implicit request  =>
      dutyCalculation(request.body).recover { case _ => InternalServerError }
  }

  private def dutyCalculation(calculationRequest: CalculationRequest)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Result] =
    customDuty(httpClient, calculationRequest)
      .fold({
        case CurrencyNotFound => NotFound("Currency not found")
        case _                => BadRequest
      }, duty => Ok(Json.toJson(duty)))
}
