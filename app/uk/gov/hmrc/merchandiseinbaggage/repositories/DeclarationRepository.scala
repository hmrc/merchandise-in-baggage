/*
 * Copyright 2024 HM Revenue & Customs
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
import javax.inject.{Inject, Singleton}
import org.mongodb.scala._
import org.mongodb.scala.model.Filters.{and, elemMatch, empty, equal, in}
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions}
import play.api.libs.json.Json._
import play.api.libs.json._
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.service.DeclarationDateOrdering
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import _root_.scala.concurrent.{ExecutionContext, Future}
import _root_.scala.util.control.NonFatal

@ImplementedBy(classOf[CryptoDeclarationRepositoryImpl])
trait DeclarationRepository {

  def insertDeclaration(declaration: Declaration): Future[Declaration]

  def upsertDeclaration(declaration: Declaration): Future[Declaration]

  def findByDeclarationId(declarationId: DeclarationId): Future[Option[Declaration]]

  def findBy(mibReference: MibReference, amendmentReference: Option[Int] = None): Future[Option[Declaration]]

  def findBy(mibReference: MibReference, eori: Eori): Future[Option[Declaration]]

  def findAll: Future[List[Declaration]]

  def deleteAll(): Future[Unit]
}

@Singleton
class DeclarationRepositoryImpl @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[Declaration](
      collectionName = "declaration",
      mongoComponent = mongo,
      domainFormat = Declaration.format,
      indexes = Seq(IndexModel(ascending(s"${Declaration.id}"), IndexOptions().name("primaryKey").unique(true))),
      replaceIndexes = false
    )
    with DeclarationDateOrdering
    with DeclarationRepository {

  implicit val jsObjectWriter: OWrites[JsObject] = (o: JsObject) => o

  def encryptDeclaration(declaration: Declaration): Declaration = declaration
  def decryptDeclaration(declaration: Declaration): Declaration = declaration
  def encryptEori(eori: Eori): Eori                             = eori

  override def insertDeclaration(declaration: Declaration): Future[Declaration] = {
    val encryptedDeclaration = encryptDeclaration(declaration)

    collection
      .insertOne(encryptedDeclaration)
      .toFuture()
      .map(_ => declaration)
      .recover {
        case NonFatal(ex)
            if ex.getMessage.contains("E11000") && ex.getMessage.contains(declaration.declarationId.value) =>
          // conflict - duplicate declaration with same declarationId
          declaration
      }
  }

  override def upsertDeclaration(declaration: Declaration): Future[Declaration] = {
    val options              = ReplaceOptions().upsert(true)
    val encryptedDeclaration = encryptDeclaration(declaration)
    collection
      .replaceOne(equal(Declaration.id, encryptedDeclaration.declarationId.value), encryptedDeclaration, options)
      .toFuture()
      .map(_ => declaration)
  }

  override def findByDeclarationId(declarationId: DeclarationId): Future[Option[Declaration]] =
    collection
      .find(equal(Declaration.id, declarationId.value))
      .toFuture()
      .map(_.headOption)
      .map(_.map(decryptDeclaration))

  override def findBy(
    mibReference: MibReference,
    amendmentReference: Option[Int] = None
  ): Future[Option[Declaration]] = {
    val query =
      amendmentReference match {
        case Some(reference) =>
          and(
            equal("mibReference", mibReference.value),
            elemMatch("amendments", equal("reference", reference))
          )

        case None => equal("mibReference", mibReference.value)
      }
    collection.find(query).toFuture().map(_.headOption).map(_.map(decryptDeclaration))
  }

  def findBy(mibReference: MibReference, eori: Eori): Future[Option[Declaration]] = {
    val encryptedEori: Eori = encryptEori(eori)
    val query               =
      and(
        equal("mibReference", mibReference.value),
        equal("eori.value", Codecs.toBson(Json.obj("$in" -> Json.arr(eori.value, encryptedEori.value))))
      )

    collection.find(query).toFuture().map(_.headOption).map {
      case Some(declaration) if declaration.encrypted.contains(true) => Some(decryptDeclaration(declaration))
      case Some(declaration)                                         => Some(declaration)
      case None                                                      => None
    }
  }

  override def findAll: Future[List[Declaration]] = collection.find().toFuture().map(_.toList.map(decryptDeclaration))

  override def deleteAll(): Future[Unit] = collection.deleteMany(empty()).toFuture().map(_ => ())
}
