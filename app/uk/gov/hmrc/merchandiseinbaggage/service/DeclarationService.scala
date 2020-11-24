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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationRequest}
import uk.gov.hmrc.merchandiseinbaggage.model.core._

import scala.concurrent.{ExecutionContext, Future}

trait DeclarationService extends Auditor {
  def persistDeclaration(persist: Declaration => Future[Declaration], declarationRequest: DeclarationRequest)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Declaration] =
    for {
      declaration <- persist(declarationRequest.toDeclaration)
      _ <- auditDeclarationComplete(declaration)
    } yield declaration

  def findByDeclarationId(findById: DeclarationId => Future[Option[Declaration]], declarationId: DeclarationId)
                         (implicit ec: ExecutionContext): EitherT[Future, BusinessError, Declaration] =
    EitherT.fromOptionF(findById(declarationId), DeclarationNotFound)
}
