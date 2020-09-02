/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

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

  def updatePaymentStatus(findByDeclarationId: DeclarationId => Future[Option[Declaration]],
                          updateStatus: (Declaration, PaymentStatus) => Future[Declaration],
                          declarationId: DeclarationId,
                          paymentStatus: PaymentStatus)
                          (implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    {

      for {
        declaration <- EitherT.fromOptionF(findByDeclarationId(declarationId), DeclarationNotFound)
        _           <- EitherT.fromEither[Future](validateNewStatus(declaration, paymentStatus).value)
        update      <- EitherT.liftF(updateStatus(declaration, paymentStatus))
      } yield update
    }
}
