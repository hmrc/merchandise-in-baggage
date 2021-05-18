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

package uk.gov.hmrc.merchandiseinbaggage.repositories

import uk.gov.hmrc.merchandiseinbaggage.model.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationRepositoryCryptoImpl @Inject()(dr: DeclarationRepositoryImpl, cryp: DeclarationCrypto)(implicit ec: ExecutionContext)
    extends DeclarationRepository {

  import cryp._

  override def insertDeclaration(declaration: Declaration): Future[Declaration] =
    dr.insertDeclaration(encryptDeclaration(declaration)).map(_ => declaration)

  override def upsertDeclaration(declaration: Declaration): Future[Declaration] =
    dr.upsertDeclaration(encryptDeclaration(declaration)).map(_ => declaration)

  override def findByDeclarationId(declarationId: DeclarationId): Future[Option[Declaration]] =
    dr.findByDeclarationId(declarationId).map(_.map(decryptDeclaration))

  override def findBy(mibReference: MibReference, amendmentReference: Option[Int]): Future[Option[Declaration]] =
    dr.findBy(mibReference, amendmentReference).map(_.map(decryptDeclaration))

  override def findBy(mibReference: MibReference, eori: Eori): Future[Option[Declaration]] =
    dr.findBy(mibReference, eori).map(_.map(decryptDeclaration))

  override def findLatestBySessionId(sessionId: SessionId): Future[Declaration] =
    dr.findLatestBySessionId(sessionId).map(decryptDeclaration)

  override def findAll: Future[List[Declaration]] =
    dr.findAll.map(_.map(decryptDeclaration))

  override def deleteAll(): Future[Unit] = dr.deleteAll()
}
