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
import cats.instances.future._
import com.google.inject.Inject
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.EmailConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}

class DeclarationService @Inject()(
                                    declarationRepository: DeclarationRepository,
                                    emailConnector: EmailConnector,
                                    val auditConnector: AuditConnector)(implicit val appConfig: AppConfig) extends Auditor {

  def persistDeclaration(declarationRequest: DeclarationRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Declaration] =
    for {
      declaration <- declarationRepository.insertDeclaration(declarationRequest.toDeclaration)
      _ <- auditDeclarationComplete(declaration)
    } yield declaration

  def findByDeclarationId(declarationId: DeclarationId)
                         (implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    EitherT.fromOptionF(declarationRepository.findByDeclarationId(declarationId), DeclarationNotFound)

  def sendEmails(declarationId: DeclarationId)(implicit hc: HeaderCarrier, ec: ExecutionContext):EitherT[Future, BusinessError, Int] = {
    findByDeclarationId(declarationId)
      .map(_.toEmailInfo(appConfig.bfEmail))
      .semiflatMap(emailConnector.sendEmails)
    //TODO: Log and alert PagerDuty for unexpected response codes
  }
}
