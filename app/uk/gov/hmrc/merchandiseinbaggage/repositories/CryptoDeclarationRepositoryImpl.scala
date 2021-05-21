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

import play.api.Configuration
import reactivemongo.api.DB
import uk.gov.hmrc.crypto.{Crypted, CryptoWithKeysFromConfig, PlainText}
import uk.gov.hmrc.merchandiseinbaggage.model.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CryptoDeclarationRepositoryImpl @Inject()(mongo: () => DB, configuration: Configuration)(implicit ec: ExecutionContext)
    extends DeclarationRepositoryImpl(mongo) {

  def insert(declaration: Declaration): Future[Declaration] = insertDeclaration(declaration)

  override def insertDeclaration(declaration: Declaration): Future[Declaration] =
    super.insertDeclaration(encryptDeclaration(declaration)).map(_ => declaration)

  override def upsertDeclaration(declaration: Declaration): Future[Declaration] =
    super.upsertDeclaration(encryptDeclaration(declaration)).map(_ => declaration)

  override def findByDeclarationId(declarationId: DeclarationId): Future[Option[Declaration]] =
    super.findByDeclarationId(declarationId).map(_.map(decryptDeclaration))

  override def findBy(mibReference: MibReference, amendmentReference: Option[Int] = None): Future[Option[Declaration]] =
    super.findBy(mibReference, amendmentReference).map(_.map(decryptDeclaration))

  override def findBy(mibReference: MibReference, eori: Eori): Future[Option[Declaration]] =
    super
      .findBy(mibReference, eori, encryptEori(eori))
      .map(x =>
        x.map {
          case r if r.eori == eori => r // mongo version was not encrypted
          case r                   => decryptDeclaration(r)
      })

  override def findLatestBySessionId(sessionId: SessionId): Future[Declaration] =
    super.findLatestBySessionId(sessionId).map(decryptDeclaration)

  override def findAll: Future[List[Declaration]] =
    super.findAll.map(_.map(decryptDeclaration))

  private lazy val crypto = new CryptoWithKeysFromConfig("mongodb.encryption", configuration.underlying)

  def encryptDeclaration(declaration: Declaration): Declaration = convertDeclaration(declaration.copy(encrypted = Some(true)), encrypt)

  def decryptDeclaration(declaration: Declaration): Declaration =
    if (declaration.encrypted.contains(true)) convertDeclaration(declaration, decrypt) else declaration

  def encryptEori(eori: Eori): Eori = eori.copy(value = encrypt(eori.value))

  private def convertDeclaration(declaration: Declaration, convert: String => String): Declaration = {
    val convertedName =
      Name(convert(declaration.nameOfPersonCarryingTheGoods.firstName), convert(declaration.nameOfPersonCarryingTheGoods.lastName))

    val convertedEmail = declaration.email.map(declarationEmail => declarationEmail.copy(email = convert(declarationEmail.email)))

    val convertedEori = declaration.eori.copy(value = convert(declaration.eori.value))

    val convertedMaybeCustomsAgent = declaration.maybeCustomsAgent.map { agent =>
      val convertedAddr =
        agent.address.copy(lines = agent.address.lines.map(convert), postcode = agent.address.postcode.map(convert))
      agent.copy(name = convert(agent.name), address = convertedAddr)
    }

    val convertedJourneyDetails = declaration.journeyDetails match {
      case journeyInSmallVehicle: JourneyInSmallVehicle =>
        journeyInSmallVehicle.copy(registrationNumber = convert(journeyInSmallVehicle.registrationNumber))
      case otherJourney => otherJourney
    }

    declaration.copy(
      nameOfPersonCarryingTheGoods = convertedName,
      email = convertedEmail,
      eori = convertedEori,
      journeyDetails = convertedJourneyDetails,
      maybeCustomsAgent = convertedMaybeCustomsAgent,
    )
  }

  private def encrypt(value: String): String =
    crypto.encrypt(PlainText(value)).value

  private def decrypt(encrypted: String): String =
    crypto.decrypt(Crypted(encrypted)).value

}
