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

import cats.data.EitherT
import cats.implicits._
import com.google.inject.Inject
import play.api.Logging
import play.api.i18n.Messages
import play.mvc.Http.Status.ACCEPTED
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.EmailConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, MibReference}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.util.PagerDutyHelper
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class DeclarationService @Inject()(
                                    declarationRepository: DeclarationRepository,
                                    emailConnector: EmailConnector,
                                    val auditConnector: AuditConnector)(implicit val appConfig: AppConfig) extends Auditor with Logging {

  def persistDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Declaration] =
    declarationRepository.insertDeclaration(declaration)
      .andThen {
        case Success(declaration) if declaration.declarationType == Export =>
          auditDeclarationComplete(declaration)
      }

  def upsertDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    EitherT(declarationRepository.upsertDeclaration(declaration).map[Either[BusinessError, Declaration]](Right(_)))

  def findByDeclarationId(declarationId: DeclarationId)
                         (implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    EitherT.fromOptionF(declarationRepository.findByDeclarationId(declarationId), DeclarationNotFound)

  def findByMibReference(mibReference: MibReference)
                        (implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    EitherT.fromOptionF(declarationRepository.findByMibReference(mibReference), DeclarationNotFound)

  def sendEmails(declarationId: DeclarationId)(implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages): EitherT[Future, BusinessError, Unit] = {
    for {
      declaration <- findByDeclarationId(declarationId)
      emailResult <- sendEmails(declaration)
    } yield emailResult
  }

  private def sendEmails(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages) = {
    val result: Future[Either[BusinessError, Unit]] = {
      if (declaration.emailsSent) {
        logger.warn(s"emails are already sent for declaration: ${declaration.mibReference.value}")
        Future.successful(Right(()))
      } else {
        val emailToBF = emailConnector.sendEmails(declaration.toEmailInfo(appConfig.bfEmail, toBorderForce = true))
        val emailToTrader = emailConnector.sendEmails(declaration.toEmailInfo(declaration.email.email))

        (emailToBF, emailToTrader).mapN { (bfResponse, trResponse) =>
          (bfResponse, trResponse) match {
            case (ACCEPTED, ACCEPTED) =>
              declarationRepository.upsertDeclaration(declaration.copy(emailsSent = true))
                .map(_ => Right(()))
            case (s1, s2) =>
              val message = s"Error in sending emails, bfResponse:$s1, trResponse:$s2"
              PagerDutyHelper.alert(Some(message))
              Future.successful(Left(EmailSentError(message)))
          }
        }.flatten
      }
    }

    EitherT(result)
  }

  def processPaymentCallback(mibRef: MibReference)(implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages): EitherT[Future, BusinessError, Unit] = {
    for {
      foundDeclaration <- findByMibReference(mibRef)
      updatedDeclaration <- upsertDeclaration(foundDeclaration.copy(paymentSuccess = Some(true)))
      emailResponse <- sendEmails(updatedDeclaration)
      _ <- EitherT(auditDeclarationComplete(updatedDeclaration).map[Either[BusinessError, Unit]](_ => Right(())))
    } yield {
      emailResponse
    }
  }
}
