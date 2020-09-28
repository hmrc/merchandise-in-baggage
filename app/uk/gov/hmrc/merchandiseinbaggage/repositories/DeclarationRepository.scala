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

package uk.gov.hmrc.merchandiseinbaggage.repositories

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.play.json.ImplicitBSONHandlers.JsObjectDocumentWriter
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Declaration, DeclarationId, PaymentStatus}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class DeclarationRepository @Inject()(mongo: () => DB)
  extends ReactiveRepository[Declaration, String]("declaration", mongo, Declaration.format, implicitly[Format[String]]) {

  override def indexes: Seq[Index] = Seq(
    Index(Seq(s"${Declaration.id}" -> Ascending), Option("primaryKey"), unique = true)
  )

  def insert(declaration: Declaration): Future[Declaration] = super.insert(declaration).map(_ => declaration)

  def findByDeclarationId(declarationId: DeclarationId): Future[Option[Declaration]] = {
    val query: (String, JsValueWrapper) = s"${Declaration.id}" -> JsString(declarationId.value)
    find(query).map(_.headOption)
  }

  def findAll: Future[List[Declaration]] = super.findAll()

  def updateStatus(declaration: Declaration, paymentStatus: PaymentStatus): Future[Declaration] = {
    val selector = Json.obj(s"${Declaration.id}" -> JsString(s"${declaration.declarationId.value}"))
    val updatedDeclaration = declaration.copy(paymentStatus = paymentStatus)
    val modifier = Json.obj("$set" -> updatedDeclaration)

    collection.update(ordered = false)
      .one(selector, modifier, upsert = true)
      .map { lastError =>
        lastError.ok
      }.map(_ => updatedDeclaration)
  }

  //TODO do we want to take some measure to stop getting called in prod!? Despite being in protected zone
  def deleteAll(): Future[Unit] = super.removeAll().map(_ => ())
}
