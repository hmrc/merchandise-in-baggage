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

package uk.gov.hmrc.merchandiseinbaggage.model.api

import java.time.LocalDate
import play.api.libs.json.*

import java.time.format.DateTimeParseException

case class JourneyDetailsEntry(portCode: String, dateOfTravel: LocalDate)

object JourneyDetailsEntry {
  implicit val format: OFormat[JourneyDetailsEntry] = new OFormat[JourneyDetailsEntry] {
    override def reads(json: JsValue): JsResult[JourneyDetailsEntry] =
      for {
        portCode     <- (json \ "portCode").validate[String].flatMap { pc =>
                          if (pc.nonEmpty) JsSuccess(pc)
                          else JsError("portCode cannot be empty")
                        }
        dateOfTravel <- (json \ "dateOfTravel").validate[String].flatMap { dateString =>
                          try
                            JsSuccess(LocalDate.parse(dateString))
                          catch {
                            case _: DateTimeParseException => JsError("Invalid date format")
                          }
                        }
      } yield JourneyDetailsEntry(portCode, dateOfTravel)

    override def writes(o: JourneyDetailsEntry): JsObject = Json.obj(
      "portCode"     -> o.portCode,
      "dateOfTravel" -> o.dateOfTravel.toString
    )
  }
}
