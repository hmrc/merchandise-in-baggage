/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import java.time.LocalDateTime

import cats.data.EitherT
import cats.instances.future._
import uk.gov.hmrc.merchandiseinbaggage.model.api.PaymentRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait PaymentService extends PaymentStatusValidator {

  def persistDeclaration(persist: Declaration => Future[Declaration], paymentRequest: PaymentRequest)
                        (implicit ec: ExecutionContext): Future[Declaration] =
    for {
      declaration <- Future.fromTry(Try(paymentRequest.toDeclarationInInitialState))
      persisted   <- persist(declaration)
    } yield persisted

  def findByDeclarationId(findById: DeclarationId => Future[Option[Declaration]], declarationId: DeclarationId)
                         (implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    EitherT.fromOptionF(findById(declarationId), DeclarationNotFound)

  def updatePaymentStatus(findByDeclarationId: DeclarationId => Future[Option[Declaration]],
                          updateStatus: (Declaration, PaymentStatus) => Future[Declaration],
                          declarationId: DeclarationId, paymentStatus: PaymentStatus)
                          (implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    for {
      declaration <- EitherT.fromOptionF(findByDeclarationId(declarationId), DeclarationNotFound)
      _           <- EitherT.fromEither[Future](validateNewStatus(declaration, paymentStatus).value)
      update      <- EitherT.liftF(updateStatus(statusUpdateTime(paymentStatus, declaration), paymentStatus))
    } yield update

  protected def statusUpdateTime(paymentStatus: PaymentStatus, declaration: Declaration): Declaration = {
    val now = LocalDateTime.now
    paymentStatus match {
      case Paid       => declaration.copy(paid = Some(now))
      case Reconciled => declaration.copy(reconciled = Some(now))
      case _          => declaration
    }
  }
}
