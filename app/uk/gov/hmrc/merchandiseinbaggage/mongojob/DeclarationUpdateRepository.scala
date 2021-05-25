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

package uk.gov.hmrc.merchandiseinbaggage.mongojob

import com.google.inject.{Inject, Singleton}
import play.api.libs.json._
import play.api.{Configuration, Logger}
import reactivemongo.api.Cursor.FailOnError
import reactivemongo.api.{DB, ReadPreference}
import reactivemongo.play.json._
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.repositories.CryptoDeclarationRepositoryImpl
import uk.gov.hmrc.merchandiseinbaggage.util.Utils.FutureOps

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class DeclarationUpdateRepository @Inject()(mongo: () => DB, configuration: Configuration)(implicit ec: ExecutionContext)
    extends CryptoDeclarationRepositoryImpl(mongo, configuration) {

  private val log = Logger(getClass)

  def transformDeclarations(): Future[Unit] = {
    log.warn("inside transformDeclarations")
    val query =
      Json.obj(
        "dateOfDeclaration" -> Json.parse("""{"$gte": "2021-01-01T00:00:00.001"}"""),
        "encrypted"         -> Json.parse("""{"$exists": false}"""),
        "amendments.goods.goods.categoryQuantityOfGoods" -> Json.parse("""{"$exists": false}""")
      )

    Try {
      collection
        .find(query)
        .cursor[Declaration](ReadPreference.primaryPreferred)
        .collect(maxDocs = 10, FailOnError[List[Declaration]]())
        .map { list =>
          list.foreach { declaration =>
            transformDeclaration(declaration)
          }
        }
        .recover {
          case ex =>
            log.warn(s"error encrypting declarations", ex)
        }
    } match {
      case Success(value) => value
      case Failure(ex) =>
        log.warn(s"error loading declarations:", ex)
        ().asFuture
    }
  }

  private def transformDeclaration(declaration: Declaration) = {
    val declarationId = declaration.declarationId.value
    val updated = encryptDeclaration(declaration)
    collection
      .update(ordered = false)
      .one(Json.obj("declarationId" -> declarationId), updated, upsert = true)
      .map { _ =>
        log.warn(s"Successfully upserted declaration, declarationId: $declarationId")
        updated
      }
  }
}
