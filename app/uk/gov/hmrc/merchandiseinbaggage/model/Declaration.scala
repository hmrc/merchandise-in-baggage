/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model

import java.time.LocalDateTime

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggage.util.ValueClassFormat


case class TraderName(value: String)
object TraderName {
  implicit val format: Format[TraderName] = ValueClassFormat.format(value => TraderName.apply(value))(_.value)
}


case class Amount(value: Long)
object Amount {
  implicit val format: Format[Amount] = ValueClassFormat.formatLong(value => Amount.apply(value))(_.value)
}


case class CsgTpsProviderId(value: String)
object CsgTpsProviderId {
  implicit val format: Format[CsgTpsProviderId] = ValueClassFormat.format(value => CsgTpsProviderId.apply(value))(_.value)
}

case class ChargeReference(value: String)
object ChargeReference {
  implicit val format: Format[ChargeReference] = ValueClassFormat.format(value => ChargeReference.apply(value))(_.value)
}


case class Declaration(id: String, name: TraderName, amount: Amount, csgTpsProviderId: CsgTpsProviderId, reference: ChargeReference) //TODO find out id
object Declaration {
  implicit val format: Format[Declaration] = Json.format
}
