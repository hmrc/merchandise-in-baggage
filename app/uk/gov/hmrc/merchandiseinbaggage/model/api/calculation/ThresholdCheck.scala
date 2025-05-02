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

package uk.gov.hmrc.merchandiseinbaggage.model.api.calculation

import play.api.libs.json.*

sealed trait ThresholdCheck
case object OverThreshold extends ThresholdCheck
case object WithinThreshold extends ThresholdCheck

object ThresholdCheck {
  given format: Format[ThresholdCheck] = new Format[ThresholdCheck] {
    override def reads(json: JsValue): JsResult[ThresholdCheck] =
      json match {
        case JsString("OverThreshold")   => JsSuccess(OverThreshold)
        case JsString("WithinThreshold") => JsSuccess(WithinThreshold)
        case JsString(other)             => JsError(s"Unknown ThresholdCheck value: $other")
        case _                           => JsError("ThresholdCheck must be a string")
      }

    override def writes(o: ThresholdCheck): JsValue = o match {
      case OverThreshold   => JsString("OverThreshold")
      case WithinThreshold => JsString("WithinThreshold")
    }
  }
}
