/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.core

import java.util.UUID.randomUUID

import play.api.libs.json._
import play.api.mvc.PathBindable
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

  implicit def pathBinder(implicit stringBinder: PathBindable[String]): PathBindable[DeclarationId] =
    new PathBindable[DeclarationId] {
      private def parseString(str: String) =
        JsString(str).validate[DeclarationId] match {
          case JsSuccess(a, _) => Right(a)
          case JsError(error)  => Left(s"No valid value in path: $str. Error: $error")
        }

      override def bind(key: String, value: String): Either[String, DeclarationId] = stringBinder.bind(key, value).right.flatMap(parseString)

      override def unbind(key: String, declarationId: DeclarationId): String = stringBinder.unbind(key, declarationId.value)
    }

}

case class Declaration(declarationId: DeclarationId, name: TraderName, amount: AmountInPence, csgTpsProviderId: CsgTpsProviderId, reference: ChargeReference)

object Declaration {
  def apply(name: TraderName, amount: AmountInPence, csgTpsProviderId: CsgTpsProviderId, reference: ChargeReference): Declaration =
    Declaration(DeclarationId(), name, amount, csgTpsProviderId, reference)

  val id = "declarationId"

  implicit val format: Format[Declaration] = Json.format
}
