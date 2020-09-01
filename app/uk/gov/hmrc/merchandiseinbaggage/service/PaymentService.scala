/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import cats.data.OptionT
import cats.implicits._
import uk.gov.hmrc.merchandiseinbaggage.model.api.PaymentRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Declaration, DeclarationId, PaymentStatus}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait PaymentService {

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
                          (implicit ec: ExecutionContext): OptionT[Future, Declaration] =
    for {
      declaration <- OptionT(findByDeclarationId(declarationId))
      update      <- OptionT.liftF(updateStatus(declaration, paymentStatus))
    } yield update
}
