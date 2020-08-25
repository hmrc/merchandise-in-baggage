/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.repositories

import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DB
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import uk.gov.hmrc.merchandiseinbaggage.model.Declaration
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

@Singleton
//class DeclarationRepository @Inject()(mongo: () => DB) extends ReactiveRepository[Declaration, String]("declaration", mongo, Declaration.format, implicitly[Format[String]]) {
class DeclarationRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent)
  extends ReactiveRepository[Declaration, String]("declaration", reactiveMongoComponent.mongoConnector.db, Declaration.format, implicitly[Format[String]]) {

  //  reactiveMongoComponent.mongoConnector.db
  override def indexes: Seq[Index] = Seq(
    Index(key = Seq("createdOn" -> IndexType.Ascending), name = Some("createdOnTime"), options = BSONDocument("expireAfterSeconds" -> "60"))
  )

  def insert(declaration: Declaration): Future[Declaration] = super.insert(declaration).map(_ => declaration)

  def findById(declarationId: String): Future[Option[Declaration]] = super.findById(declarationId)
  def findAll: Future[List[Declaration]] = super.findAll()
}
