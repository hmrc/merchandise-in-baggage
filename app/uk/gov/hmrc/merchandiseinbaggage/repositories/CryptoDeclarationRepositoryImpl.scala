/*
 * Copyright 2023 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.crypto.SymmetricCryptoFactory.aesCryptoFromConfig
import uk.gov.hmrc.crypto.{Crypted, PlainText}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.ExecutionContext

@Singleton
class CryptoDeclarationRepositoryImpl @Inject() (mongo: MongoComponent, configuration: Configuration)(implicit
  ec: ExecutionContext
) extends DeclarationRepositoryImpl(mongo) {

  private lazy val crypto = aesCryptoFromConfig("mongodb.encryption", configuration.underlying)

  override def encryptDeclaration(declaration: Declaration): Declaration =
    convertDeclaration(declaration.copy(encrypted = Some(true)), encrypt)

  override def decryptDeclaration(declaration: Declaration): Declaration =
    if (declaration.encrypted.contains(true)) convertDeclaration(declaration, decrypt) else declaration

  override def encryptEori(eori: Eori): Eori = eori.copy(value = encrypt(eori.value))

  private def convertDeclaration(declaration: Declaration, convert: String => String): Declaration = {
    val convertedName =
      Name(
        convert(declaration.nameOfPersonCarryingTheGoods.firstName),
        convert(declaration.nameOfPersonCarryingTheGoods.lastName)
      )

    val convertedEmail =
      declaration.email.map(declarationEmail => declarationEmail.copy(email = convert(declarationEmail.email)))

    val convertedEori = declaration.eori.copy(value = convert(declaration.eori.value))

    val convertedMaybeCustomsAgent = declaration.maybeCustomsAgent.map { agent =>
      val convertedAddr =
        agent.address.copy(lines = agent.address.lines.map(convert), postcode = agent.address.postcode.map(convert))
      agent.copy(name = convert(agent.name), address = convertedAddr)
    }

    val convertedJourneyDetails = declaration.journeyDetails match {
      case journeyInSmallVehicle: JourneyInSmallVehicle =>
        journeyInSmallVehicle.copy(registrationNumber = convert(journeyInSmallVehicle.registrationNumber))
      case otherJourney                                 => otherJourney
    }

    declaration.copy(
      nameOfPersonCarryingTheGoods = convertedName,
      email = convertedEmail,
      eori = convertedEori,
      journeyDetails = convertedJourneyDetails,
      maybeCustomsAgent = convertedMaybeCustomsAgent
    )
  }

  private def encrypt(value: String): String =
    crypto.encrypt(PlainText(value)).value

  private def decrypt(encrypted: String): String =
    crypto.decrypt(Crypted(encrypted)).value

}
