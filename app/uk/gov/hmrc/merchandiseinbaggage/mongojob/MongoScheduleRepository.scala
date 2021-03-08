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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import play.api.libs.json.{Format, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

case class ScheduleRecord(uid: String, runAt: LocalDateTime)

object ScheduleRecord extends ReactiveMongoFormats {
  implicit val formats: Format[ScheduleRecord] = Json.format[ScheduleRecord]
}
@ImplementedBy(classOf[MongoScheduleRepository])
trait ScheduleRepository {
  def read()(implicit ec: ExecutionContext): Future[ScheduleRecord]
  def write(nextUid: String, nextRunAt: LocalDateTime)(implicit ec: ExecutionContext): Future[Unit]
}

@Singleton
class MongoScheduleRepository @Inject()(mongoComponent: ReactiveMongoComponent)
    extends ReactiveRepository[ScheduleRecord, BSONObjectID](
      "declaration-update-schedule",
      mongoComponent.mongoConnector.db,
      ScheduleRecord.formats,
      ReactiveMongoFormats.objectIdFormats) with ScheduleRepository with StrictlyEnsureIndexes[ScheduleRecord, BSONObjectID] {

  import reactivemongo.play.json.ImplicitBSONHandlers._

  override def indexes =
    Seq(Index(Seq("uid" -> Ascending, "runAt" -> Ascending), unique = true))

  def read()(implicit ec: ExecutionContext): Future[ScheduleRecord] =
    find().flatMap(_.headOption match {
      case Some(record) => Future.successful(record)
      case None =>
        val record = ScheduleRecord(UUID.randomUUID().toString, LocalDateTime.now())
        insert(record).map(_ => record).recoverWith {
          case NonFatal(error) =>
            logger.warn(s"Creating ScheduleRecord failed: ${error.getMessage}")
            Future.failed(error)
        }
    })

  def write(newUid: String, newRunAt: LocalDateTime)(implicit ec: ExecutionContext): Future[Unit] =
    collection
      .update(ordered = false)
      .one(
        Json.obj(),
        Json.obj("$set" -> Json.obj("uid" -> newUid, "runAt" -> newRunAt))
      )
      .map(_.writeErrors.flatMap(_.errmsg).foreach(error => logger.warn(s"Updating uid and runAt failed with error: $error")))
}
