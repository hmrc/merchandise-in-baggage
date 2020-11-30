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

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{Format, JsString}
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, SessionId}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationId
import uk.gov.hmrc.merchandiseinbaggage.service.DeclarationDateOrdering
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DeclarationRepositoryImpl])
trait DeclarationRepository {
  def insertDeclaration(declaration: Declaration): Future[Declaration]
  def findByDeclarationId(declarationId: DeclarationId): Future[Option[Declaration]]
  def findLatestBySessionId(sessionId: SessionId): Future[Declaration]
  def findAll: Future[List[Declaration]]
  def deleteAll(): Future[Unit]
}

@Singleton
class DeclarationRepositoryImpl @Inject()(mongo: () => DB)(implicit ec: ExecutionContext)
  extends ReactiveRepository[Declaration, String]("declaration", mongo, Declaration.format, implicitly[Format[String]])
    with DeclarationDateOrdering with DeclarationRepository {

  override def indexes: Seq[Index] = Seq(
    Index(Seq(s"${Declaration.id}" -> Ascending), Option("primaryKey"), unique = true)
  )

  override def insertDeclaration(declaration: Declaration): Future[Declaration] =
    super.insert(declaration).map(_ => declaration)

  override def findByDeclarationId(declarationId: DeclarationId): Future[Option[Declaration]] = {
    val query: (String, JsValueWrapper) = s"${Declaration.id}" -> JsString(declarationId.value)
    find(query).map(_.headOption)
  }

  override def findLatestBySessionId(sessionId: SessionId): Future[Declaration] = {
    val query: (String, JsValueWrapper) = s"${Declaration.sessionId}" -> JsString(sessionId.value)
    find(query).map(latest)
  }

  override def findAll: Future[List[Declaration]] = super.findAll()

  //TODO do we want to take some measure to stop getting called in prod!? Despite being in protected zone
  override def deleteAll(): Future[Unit] = super.removeAll().map(_ => ())
}
