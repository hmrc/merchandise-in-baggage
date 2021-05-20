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

package uk.gov.hmrc.merchandiseinbaggage.repositories.crypto

import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.repositories.{DeclarationRepository, DeclarationRepositoryImpl}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationRepositoryCryptoWrapper @Inject()(
  declarationRepositoryImpl: DeclarationRepositoryImpl,
  declarationCrypto: DeclarationCrypto)(implicit ec: ExecutionContext)
    extends DeclarationRepository {

  import declarationCrypto._

  def insert(declaration: Declaration): Future[Declaration] = insertDeclaration(declaration)

  override def insertDeclaration(declaration: Declaration): Future[Declaration] =
    declarationRepositoryImpl.insertDeclaration(encryptDeclaration(declaration)).map(_ => declaration)

  override def upsertDeclaration(declaration: Declaration): Future[Declaration] =
    declarationRepositoryImpl.upsertDeclaration(encryptDeclaration(declaration)).map(_ => declaration)

  override def findByDeclarationId(declarationId: DeclarationId): Future[Option[Declaration]] =
    declarationRepositoryImpl.findByDeclarationId(declarationId).map(_.map(decryptDeclaration))

  override def findBy(mibReference: MibReference, amendmentReference: Option[Int]): Future[Option[Declaration]] =
    declarationRepositoryImpl.findBy(mibReference, amendmentReference).map(_.map(decryptDeclaration))

  override def findBy(mibReference: MibReference, eori: Eori): Future[Option[Declaration]] =
    declarationRepositoryImpl.findBy(mibReference, encryptEori(eori)).map(x => x.map(r => decryptDeclaration(r)))

  override def findLatestBySessionId(sessionId: SessionId): Future[Declaration] =
    declarationRepositoryImpl.findLatestBySessionId(sessionId).map(decryptDeclaration)

  override def findAll: Future[List[Declaration]] =
    declarationRepositoryImpl.findAll.map(_.map(decryptDeclaration))

  override def deleteAll(): Future[Unit] = declarationRepositoryImpl.deleteAll()

}
