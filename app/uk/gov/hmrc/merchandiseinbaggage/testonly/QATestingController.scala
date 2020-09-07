/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.testonly

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class QATestingController @Inject()(mcc: MessagesControllerComponents,
                                    declarationRepository: DeclarationRepository)(implicit val ec: ExecutionContext)
  extends BackendController(mcc) {

  def onTestDelete: Action[AnyContent] = Action(parse.default).async { _  =>
    Future.successful(declarationRepository.removeAll()).map(_ => NoContent)
  }
}
