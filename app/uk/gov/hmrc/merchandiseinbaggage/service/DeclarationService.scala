/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import cats.data.EitherT
import cats.instances.future._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core._

import scala.concurrent.{ExecutionContext, Future}

trait DeclarationService {

  def persistDeclaration(persist: Declaration => Future[Declaration], paymentRequest: DeclarationRequest)
                        (implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    EitherT.liftF(persist(paymentRequest.toDeclarationInInitialState))

  def findByDeclarationId(findById: DeclarationId => Future[Option[Declaration]], declarationId: DeclarationId)
                         (implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    EitherT.fromOptionF(findById(declarationId), DeclarationNotFound)
}
