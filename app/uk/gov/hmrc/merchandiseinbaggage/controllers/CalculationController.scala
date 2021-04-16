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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationRequest, CalculationResult, CalculationResults}
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class CalculationController @Inject()(
  calculationService: CalculationService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def handleCalculations: Action[Seq[CalculationRequest]] = Action(parse.json[Seq[CalculationRequest]]).async { implicit request =>
    Future
      .traverse(request.body) { req =>
        calculationService.calculate(req)
      }
      .map((results: Seq[CalculationResult]) => Ok(Json.toJson(results)))
  }

  def calculationResults: Action[Seq[CalculationRequest]] = Action(parse.json[Seq[CalculationRequest]]).async { implicit request =>
    Future
      .traverse(request.body) { req =>
        calculationService.calculate(req)
      }
      .map((results: Seq[CalculationResult]) => {
        val threshold = calculationService.calculateThreshold(results, request.body.headOption.map(_.destination))
        Ok(Json.toJson(CalculationResults(results, threshold)))
      })
  }
}
