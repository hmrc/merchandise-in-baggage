/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Json, Reads, Writes}
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

case class DeclarationId(value: String)
object DeclarationId {
  implicit val format: Format[DeclarationId] = ValueClassFormat.format(value => DeclarationId.apply(value))(_.value)
}

sealed trait PaymentStatus
case object Outstanding extends PaymentStatus
case object Paid extends PaymentStatus
case object Reconciled extends PaymentStatus
object PaymentStatus {
  implicit val write = new Writes[PaymentStatus] {
    override def writes(status: PaymentStatus): JsValue = status match {
      case Outstanding => Json.toJson("OUTSTANDING")
      case Paid        => Json.toJson("PAID")
      case Reconciled  => Json.toJson("RECONCILED")
    }
  }

  implicit val read = new Reads[PaymentStatus] {
    override def reads(value: JsValue): JsResult[PaymentStatus] = value match {
      case JsString("OUTSTANDING") => JsSuccess(Outstanding)
      case JsString("PAID")        => JsSuccess(Paid)
      case JsString("RECONCILED")  => JsSuccess(Reconciled)
      case _                       => JsError("invalid value")
    }
  }
}


case class Declaration(declarationId: DeclarationId, name: TraderName, amount: Amount,
                       csgTpsProviderId: CsgTpsProviderId, reference: ChargeReference,
                       paymentStatus: PaymentStatus
                      )

object Declaration {
  val id = "declarationId"
  implicit val format: Format[Declaration] = Json.format
}
