/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class PaymentController  @Inject()(mcc: MessagesControllerComponents)(implicit val ec: ExecutionContext)
  extends BackendController(mcc) {

  def onPayment(): Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok)
  }

}
