/*
 * Copyright 2021 HM Revenue & Customs
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

import cats.data.EitherT
import cats.implicits._
import com.google.inject.Inject
import play.api.Logging
import play.api.i18n.Messages
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, MibReference}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class DeclarationService @Inject()(
  declarationRepository: DeclarationRepository,
  emailService: EmailService,
  val auditConnector: AuditConnector)(implicit val appConfig: AppConfig)
    extends Auditor with Logging {

  def persistDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Declaration] =
    declarationRepository
      .insertDeclaration(declaration)
      .andThen {
        case Success(declaration) if declaration.declarationType == Export =>
          auditDeclarationComplete(declaration)
      }

  def upsertDeclaration(
    declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    EitherT(declarationRepository.upsertDeclaration(declaration).map[Either[BusinessError, Declaration]](Right(_)))

  def findByDeclarationId(declarationId: DeclarationId)(implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    EitherT.fromOptionF(declarationRepository.findByDeclarationId(declarationId), DeclarationNotFound)

  def findByMibReference(mibReference: MibReference)(implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    EitherT.fromOptionF(declarationRepository.findByMibReference(mibReference), DeclarationNotFound)

  def sendEmails(declarationId: DeclarationId)(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, BusinessError, Unit] =
    for {
      declaration <- findByDeclarationId(declarationId)
      emailResult <- emailService.sendEmails(declaration)
    } yield emailResult

  def processPaymentCallback(
    mibRef: MibReference)(implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages): EitherT[Future, BusinessError, Unit] =
    for {
      foundDeclaration   <- findByMibReference(mibRef)
      updatedDeclaration <- upsertDeclaration(foundDeclaration.copy(paymentSuccess = Some(true)))
      emailResponse      <- emailService.sendEmails(updatedDeclaration)
      _                  <- EitherT(auditDeclarationComplete(updatedDeclaration).map[Either[BusinessError, Unit]](_ => Right(())))
      _                  <- EitherT(auditRefundableDeclaration(updatedDeclaration).map[Either[BusinessError, Unit]](_ => Right(())))
    } yield {
      emailResponse
    }
}
