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

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.google.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig

import java.time
import java.time.LocalDateTime
import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class DeclarationUpdateScheduler @Inject()(
  declarationUpdateRepository: DeclarationUpdateRepository,
  scheduleRepository: ScheduleRepository,
  actorSystem: ActorSystem)(implicit appConfig: AppConfig, ec: ExecutionContext)
    extends Logging {

  val interval = appConfig.declarationUpdateConf.interval.seconds
  val enabled = appConfig.declarationUpdateConf.enabled

  if (enabled) {
    logger.warn("DeclarationUpdateScheduler is enabled.")
    val taskActor: ActorRef = actorSystem.actorOf(Props {
      new TaskActor(
        scheduleRepository,
        declarationUpdateRepository,
        interval
      )
    })
    actorSystem.scheduler.scheduleOnce(
      interval,
      taskActor,
      "<start DeclarationUpdateScheduler>"
    )
  } else {
    logger.warn("DeclarationUpdateScheduler is not enabled.")
  }
}

class TaskActor(
  scheduleRepository: ScheduleRepository,
  declarationUpdateRepository: DeclarationUpdateRepository,
  repeatInterval: FiniteDuration)(implicit ec: ExecutionContext)
    extends Actor with Logging {

  def receive = {
    case uid: String =>
      scheduleRepository.read().flatMap {
        case ScheduleRecord(recordUid, runAt) =>
          val now = LocalDateTime.now()
          if (uid == recordUid) {
            val newUid = UUID.randomUUID().toString
            val nextRunAt = (if (runAt.isBefore(now)) now else runAt)
              .plusSeconds(repeatInterval.toSeconds + Random.nextInt(100))
            val delay = time.Duration.between(now, nextRunAt).getSeconds
            scheduleRepository
              .write(newUid, nextRunAt)
              .map(_ => {
                context.system.scheduler.scheduleOnce(delay.seconds, self, newUid)
                logger.warn(s"Starting DeclarationUpdateScheduler job, next job is scheduled at $nextRunAt")
                declarationUpdateRepository.transformDeclarations()
              })
          } else {
            val nextRunAt = if (runAt.isBefore(now)) now else runAt
            val delay = time.Duration.between(now, nextRunAt).getSeconds
            context.system.scheduler.scheduleOnce(delay.seconds, self, recordUid)
            Future.successful(())
          }
      }
      ()
  }
}
