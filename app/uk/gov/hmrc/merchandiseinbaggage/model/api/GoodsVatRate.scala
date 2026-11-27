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

enum GoodsVatRate(val value: Int) {
  case Zero extends GoodsVatRate(0)
  case Five extends GoodsVatRate(5)
  case Twenty extends GoodsVatRate(20)
}

object GoodsVatRate {
  given format: Format[GoodsVatRate] = new Format[GoodsVatRate] {
    def reads(json: JsValue): JsResult[GoodsVatRate] = json match {
      case JsString("Zero")   => JsSuccess(Zero)
      case JsString("Five")   => JsSuccess(Five)
      case JsString("Twenty") => JsSuccess(Twenty)
      case _                  => JsError("Invalid Goods Vat Rate")
    }

    def writes(goodsVatRate: GoodsVatRate): JsValue = JsString(goodsVatRate.toString)
  }
}
