/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import cats.instances.future._
import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
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

  def onCalculations(): Action[JsValue] = Action(parse.json).async { implicit request  =>
    request.body.asOpt[CalculationRequest].fold(
      Future.successful(InternalServerError("invalid")))(req =>
      dutyCalculation(req)).recover { case _ => InternalServerError }
  }

  private def dutyCalculation(calculationRequest: CalculationRequest)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Result] =
    customDuty(httpClient, CalculationRequest(calculationRequest.currency, calculationRequest.amount))
      .fold({
        case CurrencyNotFound => NotFound("Currency not found")
        case _                => BadRequest
      }, duty => Ok(Json.toJson(duty)))
}
