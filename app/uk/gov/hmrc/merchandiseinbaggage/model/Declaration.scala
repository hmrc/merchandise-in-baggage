/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model

import java.time.LocalDateTime

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.merchandiseinbaggage.util.ValueClassFormat


case class Name(value: String)
object Name {
  implicit val format: Format[Name] = ValueClassFormat.format(value => Name.apply(value))(_.value)
}


case class Amount(value: Long)
object Amount {
  implicit val format: Format[Amount] = ValueClassFormat.formatLong(value => Amount.apply(value))(_.value)
}


case class Reference(value: String)
object Reference {
  implicit val format: Format[Reference] = ValueClassFormat.format(value => Reference.apply(value))(_.value)
}


case class Declaration(id: String, name: Name, amount: Amount, reference: Reference, lastUpdated: LocalDateTime) //TODO find out id
object Declaration {
  implicit val format: Format[Declaration] = Json.format
}
