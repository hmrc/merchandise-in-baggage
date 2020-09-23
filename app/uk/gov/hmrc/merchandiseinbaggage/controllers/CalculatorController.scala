/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.model.api.CalculationRequest
import uk.gov.hmrc.merchandiseinbaggage.service.CustomsDutyCalculator
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class CalculatorController @Inject()(mcc: MessagesControllerComponents)
                                    (implicit val ec: ExecutionContext)
  extends BackendController(mcc) with CustomsDutyCalculator {

  def onCalculations(): Action[AnyContent] = Action(parse.default).async { implicit request  =>
    (for {
      parsed <- request.body.asJson
      req    <- parsed.asOpt[CalculationRequest]
    } yield req).fold(
      Future.successful(InternalServerError("invalid")))(req =>
      Future.successful(Ok(Json.toJson(customDuty(req.currency, req.amount))))
    )
  }
}
