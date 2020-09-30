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

package uk.gov.hmrc.merchandiseinbaggage.model.api

import java.util.UUID

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggage.model.core._

case class DeclarationRequest(traderName: TraderName, amount: ForeignAmount, csgTpsProviderId: CsgTpsProviderId, chargeReference: ChargeReference)
object DeclarationRequest {
  implicit val format: Format[DeclarationRequest] = Json.format

  implicit class ToDeclaration(paymentRequest: DeclarationRequest) {
    def toDeclarationInInitialState: Declaration = {
      import paymentRequest._
      Declaration(DeclarationId(UUID.randomUUID().toString), traderName, amount, csgTpsProviderId,
        chargeReference, Outstanding, None, None)
    }
  }
}

case class PaymentStatusRequest(status: PaymentStatus)
object PaymentStatusRequest {
  implicit val format: Format[PaymentStatusRequest] = Json.format
}
