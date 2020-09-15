/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import javax.inject.Inject
import play.api.libs.json.Json.toJson
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationIdResponse._
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationIdResponse, DeclarationRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class DeclarationController @Inject()(mcc: MessagesControllerComponents, repo: DeclarationRepository)(implicit val ec: ExecutionContext)
  extends BackendController(mcc) {

  val onDeclarations: Action[DeclarationRequest] = Action(parse.json[DeclarationRequest]).async { implicit request =>
    repo.insert(request.body.toDeclaration).map { persisted =>
      Created(toJson(DeclarationIdResponse(persisted.declarationId)))
    }
  }

  def onRetrieve(declarationId: DeclarationId): Action[AnyContent] = Action.async {
    repo.findByDeclarationId(declarationId).map {
      case Some(declaration) => Ok(toJson(declaration))
      case _ => NotFound
    }
  }
}
