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
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}

import scala.concurrent.{ExecutionContext, Future}

trait DeclarationService extends Auditor {
  private val logger = Logger(this.getClass)

  def persistDeclaration(persist: Declaration => Future[Declaration], declarationRequest: DeclarationRequest)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Declaration] =
    for {
      declaration <- persist(declarationRequest.toDeclaration)
      auditStatus <- auditDeclarationPersisted(declaration)
    } yield {
      auditStatus match {
        case Success =>
          logger.info(s"Successful audit of declaration with id [${declaration.declarationId}]")
        case Disabled =>
          logger.warn(s"Audit of declaration with id [${declaration.declarationId}] returned Disabled")
        case Failure(message, _) =>
          logger.error(s"Audit of declaration with id [${declaration.declarationId}] returned Failure with message [$message]")
      }

      declaration
    }

  def findByDeclarationId(findById: DeclarationId => Future[Option[Declaration]], declarationId: DeclarationId)
                         (implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    EitherT.fromOptionF(findById(declarationId), DeclarationNotFound)
}
