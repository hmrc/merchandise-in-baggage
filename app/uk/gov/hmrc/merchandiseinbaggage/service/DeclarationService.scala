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

  def persistDeclaration(persist: DeclarationBE => Future[DeclarationBE], paymentRequest: DeclarationRequest)
                        (implicit ec: ExecutionContext): Future[DeclarationBE] =
    for {
      declaration <- Future.fromTry(Try(paymentRequest.toDeclarationInInitialState))
      persisted   <- persist(declaration)
    } yield persisted

  def findByDeclarationId(findById: DeclarationId => Future[Option[DeclarationBE]], declarationId: DeclarationId)
                         (implicit ec: ExecutionContext): EitherT[Future, BusinessError, DeclarationBE] =
    EitherT.fromOptionF(findById(declarationId), DeclarationNotFound)

  def updatePaymentStatus(findByDeclarationId: DeclarationId => Future[Option[DeclarationBE]],
                          updateStatus: (DeclarationBE, PaymentStatus) => Future[DeclarationBE],
                          declarationId: DeclarationId, paymentStatus: PaymentStatus)
                          (implicit ec: ExecutionContext): EitherT[Future, BusinessError, DeclarationBE] =
    for {
      declaration <- EitherT.fromOptionF(findByDeclarationId(declarationId), DeclarationNotFound)
      _           <- EitherT.fromEither[Future](validateRequest(declaration, paymentStatus).value)
      withTime    = statusUpdateTime(paymentStatus, declaration)
      update      <- EitherT.liftF(updateStatus(withTime, paymentStatus))
    } yield update

  protected def statusUpdateTime(paymentStatus: PaymentStatus, declaration: DeclarationBE): DeclarationBE = {
    val now: LocalDateTime = generateTime
    paymentStatus match {
      case Paid       => declaration.copy(paid = Some(now))
      case Reconciled => declaration.copy(reconciled = Some(now))
      case _          => declaration
    }
  }

  protected def generateTime: LocalDateTime = LocalDateTime.now
}
