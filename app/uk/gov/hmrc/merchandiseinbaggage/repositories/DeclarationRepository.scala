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

import com.google.inject.ImplementedBy
import play.api.Configuration
import play.api.libs.json.Json.{JsValueWrapper, _}
import play.api.libs.json._
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import uk.gov.hmrc.crypto.{Crypted, CryptoWithKeysFromConfig, PlainText}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.service.DeclarationDateOrdering
import uk.gov.hmrc.mongo.ReactiveRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@ImplementedBy(classOf[DeclarationRepositoryImpl])
trait DeclarationRepository {

  def insertDeclaration(declaration: Declaration): Future[Declaration]

  def upsertDeclaration(declaration: Declaration): Future[Declaration]

  def findByDeclarationId(declarationId: DeclarationId): Future[Option[Declaration]]

  def findBy(mibReference: MibReference, amendmentReference: Option[Int] = None): Future[Option[Declaration]]

  def findBy(mibReference: MibReference, eori: Eori): Future[Option[Declaration]]

  def findLatestBySessionId(sessionId: SessionId): Future[Declaration]

  def findAll: Future[List[Declaration]]

  def deleteAll(): Future[Unit]
}

@Singleton
class DeclarationRepositoryImpl @Inject()(mongo: () => DB)(implicit ec: ExecutionContext, configuration: Configuration)
    extends ReactiveRepository[Declaration, String]("declaration", mongo, Declaration.format, implicitly[Format[String]])
    with DeclarationDateOrdering with DeclarationRepository {

  private lazy val crypto = new CryptoWithKeysFromConfig("mongodb.encryption", configuration.underlying)

  implicit val jsObjectWriter: OWrites[JsObject] = new OWrites[JsObject] {
    override def writes(o: JsObject): JsObject = o
  }

  override def indexes: Seq[Index] = Seq(
    Index(Seq(s"${Declaration.id}" -> Ascending), Option("primaryKey"), unique = true)
  )

  override def insertDeclaration(declaration: Declaration): Future[Declaration] = {
    val encryptedDeclaration = encrypt(declaration)
    super
      .insert(encryptedDeclaration)
      .map(_ => encryptedDeclaration)
      .recover {
        case NonFatal(ex) if ex.getMessage.contains("E11000") && ex.getMessage.contains(declaration.declarationId.value) =>
          //conflict - duplicate declaration with same declarationId
          declaration
      }
  }

  override def upsertDeclaration(declaration: Declaration): Future[Declaration] = {
    val encryptedDeclaration = encrypt(declaration)
    collection
      .update(ordered = false)
      .one(Json.obj(Declaration.id -> declaration.declarationId.value), encryptedDeclaration, upsert = true)
      .map(_ => encryptedDeclaration)
  }

  override def findByDeclarationId(declarationId: DeclarationId): Future[Option[Declaration]] = {
    val query: (String, JsValueWrapper) = s"${Declaration.id}" -> JsString(declarationId.value)
    find(query).map(_.headOption).map {
      case Some(declaration) => Some(decrypt(declaration))
      case None              => None

    }
  }

  override def findBy(mibReference: MibReference, amendmentReference: Option[Int] = None): Future[Option[Declaration]] = {
    val query =
      amendmentReference match {
        case Some(reference) =>
          Json.obj(
            "mibReference" -> mibReference.value,
            "amendments"   -> Json.obj("$elemMatch" -> Json.parse(s"""{"reference": $reference}"""))
          )
        case None => Json.obj("mibReference" -> mibReference.value)
      }

    collection.find(query, None).one[Declaration].map {
      case Some(declaration) => Some(decrypt(declaration))
      case None              => None
    }
  }

  override def findLatestBySessionId(sessionId: SessionId): Future[Declaration] = {
    val query: (String, JsValueWrapper) = s"${Declaration.sessionId}" -> JsString(sessionId.value)
    find(query).map(latest).map(decrypt)
  }

  override def findAll: Future[List[Declaration]] = super.findAll()

  //TODO do we want to take some measure to stop getting called in prod!? Despite being in protected zone
  override def deleteAll(): Future[Unit] = super.removeAll().map(_ => ())

  override def findBy(mibReference: MibReference, eori: Eori): Future[Option[Declaration]] = {
    val query: Seq[(String, JsValueWrapper)] = Seq(("mibReference", JsString(mibReference.value)), ("eori.value", JsString(eori.value)))
    find(query: _*).map(_.headOption).map {
      case Some(declaration) => Some(decrypt(declaration))
      case None              => None
    }
  }

  private def encrypt(value: String): String =
    crypto.encrypt(PlainText(value)).value

  private def decrypt(encrypted: String): String =
    crypto.decrypt(Crypted(encrypted)).value

  private def encrypt(declaration: Declaration): Declaration = {
    import declaration._

    def encryptedName =
      Name(encrypt(nameOfPersonCarryingTheGoods.firstName), encrypt(nameOfPersonCarryingTheGoods.lastName))

    def encryptedEmail = email.map(declarationEmail => declarationEmail.copy(email = encrypt(declarationEmail.email)))

    def encryptedEori = eori.copy(value = encrypt(eori.value))

    def encryptedCustomsAgent = maybeCustomsAgent.map { agent =>
      val encryptedAddress =
        agent.address.copy(lines = agent.address.lines.map(encrypt), postcode = agent.address.postcode.map(encrypt))
      agent.copy(name = encrypt(agent.name), address = encryptedAddress)
    }

    def encryptedJourneyDetails = journeyDetails match {
      case journeyInSmallVehicle: JourneyInSmallVehicle =>
        journeyInSmallVehicle.copy(registrationNumber = encrypt(journeyInSmallVehicle.registrationNumber))
      case otherJourney => otherJourney
    }

    if (!declaration.piiEncrypted) {
      declaration.copy(
        nameOfPersonCarryingTheGoods = encryptedName,
        email = encryptedEmail,
        eori = encryptedEori,
        journeyDetails = encryptedJourneyDetails,
        maybeCustomsAgent = encryptedCustomsAgent,
        piiEncrypted = true
      )
    } else {
      declaration
    }
  }

  private def decrypt(declaration: Declaration): Declaration = {
    if (declaration.piiEncrypted) {
      ???
    } else {
      declaration
    }
  }
}
