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
import play.api.i18n.MessagesApi
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class DeclarationService @Inject()(
  declarationRepository: DeclarationRepository,
  emailService: EmailService,
  val auditConnector: AuditConnector,
  val messagesApi: MessagesApi)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends Auditor with Logging {

  def persistDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Declaration] = {
    val updatedDeclaration = updatePaymentStatusIfNeeded(declaration)
    declarationRepository
      .insertDeclaration(updatedDeclaration)
      .andThen {
        case Success(result) if canTriggerEmailsAndAudit(result) =>
          triggerEmailsAndAudit(result)
      }
  }

  def amendDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Declaration] = {
    val updatedDeclaration = updatePaymentStatusIfNeeded(declaration)
    declarationRepository
      .upsertDeclaration(updatedDeclaration)
      .andThen {
        case Success(result) if canTriggerEmailsAndAudit(result) =>
          triggerEmailsAndAudit(result)
      }
  }

  private def canTriggerEmailsAndAudit(declaration: Declaration): Boolean =
    declaration.declarationType == Export || importWithNoPayment(declaration)

  private def updatePaymentStatusIfNeeded(declaration: Declaration) =
    declaration.declarationType match {
      case Import if newDeclarationWithNoTaxDue(declaration) =>
        declaration.copy(paymentStatus = Some(NotRequired))
      case Import if amendDeclarationWithNoTaxDue(declaration) =>
        val updatedAmendment = declaration.amendments.last.copy(paymentStatus = Some(NotRequired))
        val amendments = declaration.amendments.dropRight(1) :+ updatedAmendment
        declaration.copy(amendments = amendments)
      case _ => declaration
    }

  private def newDeclarationWithNoTaxDue(declaration: Declaration) =
    declaration.amendments.isEmpty && declaration.maybeTotalCalculationResult.exists(_.totalTaxDue.value == 0)

  private def amendDeclarationWithNoTaxDue(declaration: Declaration) =
    declaration.amendments.nonEmpty && declaration.amendments.last.maybeTotalCalculationResult.exists(_.totalTaxDue.value == 0)

  private def importWithNoPayment(declaration: Declaration) =
    declaration.declarationType == Import && (newDeclarationWithNoTaxDue(declaration) || amendDeclarationWithNoTaxDue((declaration)))

  private def triggerEmailsAndAudit(declaration: Declaration)(implicit hc: HeaderCarrier) =
    emailService
      .sendEmails(declaration)
      .fold(
        _ => auditDeclarationComplete(declaration.copy(emailsSent = false)),
        _ => auditDeclarationComplete(declaration.copy(emailsSent = true))
      )
      .flatten

  def upsertDeclaration(declaration: Declaration): EitherT[Future, BusinessError, Declaration] =
    EitherT(declarationRepository.upsertDeclaration(declaration).map[Either[BusinessError, Declaration]](Right(_)))

  def findByDeclarationId(declarationId: DeclarationId): EitherT[Future, BusinessError, Declaration] =
    EitherT.fromOptionF(declarationRepository.findByDeclarationId(declarationId), DeclarationNotFound)

  def findByMibReference(mibReference: MibReference): EitherT[Future, BusinessError, Declaration] =
    EitherT.fromOptionF(declarationRepository.findByMibReference(mibReference), DeclarationNotFound)

  def findBy(mibReference: MibReference, eori: Eori): EitherT[Future, BusinessError, Declaration] =
    EitherT.fromOptionF(declarationRepository.findBy(mibReference, eori), DeclarationNotFound)

  def processPaymentCallback(mibRef: MibReference)(implicit hc: HeaderCarrier): EitherT[Future, BusinessError, Unit] =
    for {
      foundDeclaration   <- findByMibReference(mibRef)
      updatedDeclaration <- upsertDeclaration(foundDeclaration.copy(paymentStatus = Some(Paid)))
      emailResponse      <- emailService.sendEmails(updatedDeclaration)
      _                  <- EitherT(auditDeclarationComplete(updatedDeclaration.copy(emailsSent = true)).map[Either[BusinessError, Unit]](_ => Right(())))
      _                  <- EitherT(auditRefundableDeclaration(updatedDeclaration.copy(emailsSent = true)).map[Either[BusinessError, Unit]](_ => Right(())))
    } yield {
      emailResponse
    }
}
