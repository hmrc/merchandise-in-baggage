/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.repositories

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import reactivemongo.api.DB
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import uk.gov.hmrc.merchandiseinbaggage.model.{Declaration, DeclarationId}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class DeclarationRepository @Inject()(mongo: () => DB)
  extends ReactiveRepository[Declaration, String]("declaration", mongo, Declaration.format, implicitly[Format[String]]) {

  override def indexes: Seq[Index] = Seq(
    Index(key = Seq("createdOn" -> IndexType.Ascending), name = Some("createdOnTime"), options = BSONDocument("expireAfterSeconds" -> "60")),
    Index(Seq(s"${Declaration.id}" -> Ascending), Option("primaryKey"), unique = true)
  )

  def insert(declaration: Declaration): Future[Declaration] = super.insert(declaration).map(_ => declaration)

  def findByDeclarationId(declarationId: DeclarationId): Future[List[Declaration]] = {
    val query: (String, JsValueWrapper) = s"${Declaration.id}" -> JsString(declarationId.value)
    find(query)
  }

  def findById(declarationId: String): Future[Option[Declaration]] = super.findById(declarationId)

  def findAll: Future[List[Declaration]] = super.findAll()

  //TODO do we want to take some measure to stop getting called in prod!? Despite being in protected zone
  def deleteAll: Future[Unit] = super.removeAll().map(_ => ())
}
