/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.libs.json.*

enum YesNoDontKnow {
  case Yes, No, DontKnow

  val messageKey: String = s"yndk.$this"
}

object YesNoDontKnow {
  given format: Format[YesNoDontKnow] = new Format[YesNoDontKnow] {
    def reads(json: JsValue): JsResult[YesNoDontKnow] = json match {
      case JsString("Yes")      => JsSuccess(Yes)
      case JsString("No")       => JsSuccess(No)
      case JsString("DontKnow") => JsSuccess(DontKnow)
      case _                    => JsError("Invalid Yes No Dont Know")
    }

    def writes(yesNoDontKnow: YesNoDontKnow): JsValue = JsString(yesNoDontKnow.toString)
  }
}
