/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.util

import play.api.i18n.Messages

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

  private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM YYYY, h:mma", Locale.ENGLISH)

  private def translatedDate(date: String)(implicit messages: Messages): String = {
    val result = if (messages.lang.code == "cy") {
      val englishMonth = date.split(" ")(1)
      date.replace(englishMonth, messages(s"title.${englishMonth.toLowerCase}"))
    } else {
      date
    }

    result
      .replace("AM", "am")
      .replace("PM", "pm")
  }

  implicit class LocalDateTimeOps(dateTime: LocalDateTime) {
    def formattedDate(implicit messages: Messages): String = {
      val dateFormatted = dateTime.format(dateTimeFormatter)
      translatedDate(dateFormatted)
    }
  }
}
