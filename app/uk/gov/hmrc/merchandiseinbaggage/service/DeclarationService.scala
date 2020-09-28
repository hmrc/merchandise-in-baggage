/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import java.time.LocalDateTime

import cats.data.EitherT
import cats.instances.future._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait DeclarationService extends DeclarationValidator {

  def persistDeclaration(persist: Declaration => Future[Declaration], paymentRequest: DeclarationRequest)
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
