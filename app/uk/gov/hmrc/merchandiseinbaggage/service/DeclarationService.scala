/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import java.time.LocalDateTime

import cats.Id
import cats.data.EitherT
import cats.instances.future._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core._

import scala.concurrent.{ExecutionContext, Future}

trait DeclarationService extends DeclarationValidator {

  def persistDeclaration(persist: Declaration => Future[Declaration], paymentRequest: DeclarationRequest)
                        (implicit ec: ExecutionContext): EitherT[Id, BusinessError, Declaration] =
    validatePersistRequest(paymentRequest.toDeclarationInInitialState)

  def findByDeclarationId(findById: DeclarationId => Future[Option[Declaration]], declarationId: DeclarationId)
                         (implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    EitherT.fromOptionF(findById(declarationId), DeclarationNotFound)

  def updatePaymentStatus(findByDeclarationId: DeclarationId => Future[Option[Declaration]],
                          updateStatus: (Declaration, PaymentStatus) => Future[Declaration],
                          declarationId: DeclarationId, paymentStatus: PaymentStatus)
                          (implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    for {
      declaration <- EitherT.fromOptionF(findByDeclarationId(declarationId), DeclarationNotFound)
      _           <- EitherT.fromEither[Future](validateRequest(declaration, paymentStatus).value)
      withTime    = statusUpdateTime(paymentStatus, declaration)
      update      <- EitherT.liftF(updateStatus(withTime, paymentStatus))
    } yield update

  protected def statusUpdateTime(paymentStatus: PaymentStatus, declaration: Declaration): Declaration = {
    val now: LocalDateTime = generateTime
    paymentStatus match {
      case Paid       => declaration.copy(paid = Some(now))
      case Reconciled => declaration.copy(reconciled = Some(now))
      case _          => declaration
    }
  }

  protected def generateTime: LocalDateTime = LocalDateTime.now
}
