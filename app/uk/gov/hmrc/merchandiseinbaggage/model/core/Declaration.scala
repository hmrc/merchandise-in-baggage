/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.core

import java.util.UUID.randomUUID

import play.api.libs.json._
import uk.gov.hmrc.merchandiseinbaggage.util.ValueClassFormat


case class TraderName(value: String)

object TraderName {
  implicit val format: Format[TraderName] = ValueClassFormat.format(value => TraderName.apply(value))(_.value)
}


case class AmountInPence(value: Double)

object AmountInPence {
  implicit val format: Format[AmountInPence] = ValueClassFormat.formatDouble(value => AmountInPence.apply(value))(_.value)
}


case class CsgTpsProviderId(value: String)

object CsgTpsProviderId {
  implicit val format: Format[CsgTpsProviderId] = ValueClassFormat.format(value => CsgTpsProviderId.apply(value))(_.value)
}

case class ChargeReference(value: String)

object ChargeReference {
  implicit val format: Format[ChargeReference] = ValueClassFormat.format(value => ChargeReference.apply(value))(_.value)
}

case class DeclarationId(value: String)

object DeclarationId {
  def apply(): DeclarationId = DeclarationId(randomUUID().toString)

  implicit val format: Format[DeclarationId] = ValueClassFormat.format(value => DeclarationId.apply(value))(_.value)
}

case class Declaration(declarationId: DeclarationId, name: TraderName, amount: AmountInPence, csgTpsProviderId: CsgTpsProviderId, reference: ChargeReference)

object Declaration {
  def apply(name: TraderName, amount: AmountInPence, csgTpsProviderId: CsgTpsProviderId, reference: ChargeReference): Declaration =
    new Declaration(
      declarationId = DeclarationId(),
      name = name,
      amount = amount,
      csgTpsProviderId = csgTpsProviderId,
      reference = reference)

  val id = "declarationId"

  implicit val format: Format[Declaration] = Json.format
}
