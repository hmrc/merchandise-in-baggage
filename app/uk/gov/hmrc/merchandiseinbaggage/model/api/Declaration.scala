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

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

import enumeratum.EnumEntry
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.merchandiseinbaggage.model.DeclarationEmailInfo
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationId
import uk.gov.hmrc.merchandiseinbaggage.util.Obfuscator.obfuscate

import scala.collection.immutable

case class SessionId(value: String)

object SessionId {
  implicit val format: Format[SessionId] = implicitly[Format[String]].inmap(SessionId(_), _.value)
}

case class PurchaseDetails(amount: String, currency: Currency) {
  override def toString: String =
    s"$amount, ${currency.currencyName} (${currency.currencyCode})"
}

object PurchaseDetails {
  implicit val format: OFormat[PurchaseDetails] = Json.format[PurchaseDetails]
}

case class CategoryQuantityOfGoods(category: String, quantity: String)

object CategoryQuantityOfGoods {
  implicit val format: OFormat[CategoryQuantityOfGoods] = Json.format[CategoryQuantityOfGoods]
}

case class AmountInPence(value: Long)

object AmountInPence {
  implicit val format: Format[AmountInPence] = implicitly[Format[Long]].inmap(AmountInPence(_), _.value)
}

case class GoodsEntry(maybeCategoryQuantityOfGoods: Option[CategoryQuantityOfGoods] = None,
                      maybeGoodsVatRate: Option[GoodsVatRate] = None,
                      maybeCountryOfPurchase: Option[String] = None,
                      maybePurchaseDetails: Option[PurchaseDetails] = None,
                      maybeInvoiceNumber: Option[String] = None)


case class GoodsEntries(entries: Seq[GoodsEntry])

object GoodsEntry {
  implicit val format: OFormat[GoodsEntry] = Json.format[GoodsEntry]
}

object GoodsEntries {
  implicit val format: OFormat[GoodsEntries] = Json.format[GoodsEntries]
}

case class Name(firstName: String, lastName: String) {
  override val toString: String = s"$firstName $lastName"

  lazy val obfuscated: Name = Name(obfuscate(firstName), obfuscate(lastName))
}

object Name {
  implicit val format: OFormat[Name] = Json.format[Name]
}

case class Eori(value: String) {
  override val toString: String = value

  lazy val obfuscated: Eori = Eori(obfuscate(value))
}

object Eori {
  implicit val format: OFormat[Eori] = Json.format[Eori]
}

case class JourneyDetailsEntry(placeOfArrival: Port, dateOfArrival: LocalDate)

object JourneyDetailsEntry {
  implicit val format: OFormat[JourneyDetailsEntry] = Json.format[JourneyDetailsEntry]
}

case class Goods(categoryQuantityOfGoods: CategoryQuantityOfGoods,
                 goodsVatRate: GoodsVatRate,
                 countryOfPurchase: String,
                 purchaseDetails: PurchaseDetails)

object Goods {
  implicit val format: OFormat[Goods] = Json.format[Goods]
}

case class DeclarationGoods(goods: Seq[Goods])

object DeclarationGoods {
  implicit val format: OFormat[DeclarationGoods] = Json.format[DeclarationGoods]
}

case class CustomsAgent(name: String, address: Address) {
  lazy val obfuscated: CustomsAgent = CustomsAgent(obfuscate(name), address.obfuscated)
}

object CustomsAgent {
  implicit val format: OFormat[CustomsAgent] = Json.format[CustomsAgent]
}

sealed trait YesNo extends EnumEntry

object YesNo extends Enum[YesNo] {
  override val values: immutable.IndexedSeq[YesNo] = findValues

  def from(bool: Boolean): YesNo = if (bool) Yes else No

  def to(yesNo: YesNo): Boolean = yesNo match {
    case Yes => true
    case No => false
  }

  case object Yes extends YesNo

  case object No extends YesNo

}


sealed trait JourneyDetails {
  val placeOfArrival: Port
  val dateOfArrival: LocalDate
  val formattedDateOfArrival: String = DateTimeFormatter.ofPattern("dd MMM yyyy").format(dateOfArrival)
  val travellingByVehicle: YesNo = No
  val maybeRegistrationNumber: Option[String] = None

  def obfuscated: JourneyDetails = this
}

case class JourneyViaFootPassengerOnlyPort(placeOfArrival: FootPassengerOnlyPort, dateOfArrival: LocalDate) extends JourneyDetails

case class JourneyOnFootViaVehiclePort(placeOfArrival: VehiclePort, dateOfArrival: LocalDate) extends JourneyDetails

case class JourneyInSmallVehicle(placeOfArrival: VehiclePort, dateOfArrival: LocalDate, registrationNumber: String) extends JourneyDetails {
  override val travellingByVehicle: YesNo = Yes
  override val maybeRegistrationNumber: Option[String] = Some(registrationNumber)
  override lazy val obfuscated: JourneyInSmallVehicle = this.copy(registrationNumber = obfuscate(registrationNumber))
}

object JourneyDetails {
  implicit val format: OFormat[JourneyDetails] = Json.format[JourneyDetails]
}

object JourneyViaFootPassengerOnlyPort {
  implicit val format: OFormat[JourneyViaFootPassengerOnlyPort] = Json.format[JourneyViaFootPassengerOnlyPort]
}

object JourneyOnFootViaVehiclePort {
  implicit val format: OFormat[JourneyOnFootViaVehiclePort] = Json.format[JourneyOnFootViaVehiclePort]
}

object JourneyInSmallVehicle {
  implicit val format: Format[JourneyInSmallVehicle] = Json.format[JourneyInSmallVehicle]
}

case class Declaration(declarationId: DeclarationId,
                       sessionId: SessionId,
                       declarationType: DeclarationType,
                       goodsDestination: GoodsDestination,
                       declarationGoods: DeclarationGoods,
                       nameOfPersonCarryingTheGoods: Name,
                       email: Email,
                       maybeCustomsAgent: Option[CustomsAgent],
                       eori: Eori,
                       journeyDetails: JourneyDetails,
                       dateOfDeclaration: LocalDateTime,
                       mibReference: MibReference,
                      ) {
  lazy val obfuscated: Declaration =
    this.copy(
      nameOfPersonCarryingTheGoods = nameOfPersonCarryingTheGoods.obfuscated,
      email = email.obfuscated,
      maybeCustomsAgent = maybeCustomsAgent.map(_.obfuscated),
      eori = eori.obfuscated,
      journeyDetails = journeyDetails.obfuscated
    )

  def toEmailInfo(bfEmail: String): DeclarationEmailInfo = {
    val templateId = if (declarationType == DeclarationType.Import) "mods_import_declaration" else "mods_export_declaration"
    val goodsParams = declarationGoods.goods.zipWithIndex.map { goodsWithIdx =>
      val (goods, idx) = goodsWithIdx
      val countryOrDestKey =  if (declarationType == DeclarationType.Import) s"goodsCountry_$idx" else s"goodsDestination_$idx"
      Map(
        s"goodsCategory_$idx" -> goods.categoryQuantityOfGoods.category,
        s"goodsQuantity_$idx" -> goods.categoryQuantityOfGoods.quantity,
        countryOrDestKey -> goods.countryOfPurchase,
        s"goodsPrice_$idx" -> goods.purchaseDetails.toString,
      )
    }.reduce(_ ++ _)

    val commonParams = Map(
      "nameOfPersonCarryingGoods" -> nameOfPersonCarryingTheGoods.toString,
      "declarationReference" -> mibReference.value,
      "dateOfDeclaration" -> dateOfDeclaration.toString,
      "eori" -> eori.value
    )

    val calculationParams = Map(
      "customsDuty" -> "???",
      "vat" -> "???",
      "total" -> "???"
    )

    val allParams =
      if (declarationType == DeclarationType.Import)
        goodsParams ++ commonParams ++ calculationParams
      else
        goodsParams ++ commonParams

    DeclarationEmailInfo(
      Seq(email.email, bfEmail),
      templateId,
      allParams
    )
  }
}

object Declaration {
  val id = "declarationId"
  val sessionId = "sessionId"
  implicit val format: OFormat[Declaration] = Json.format[Declaration]
}

sealed trait GoodsVatRate extends EnumEntry {
  val value: Int
}

object GoodsVatRate {
  implicit val format: Format[GoodsVatRate] = EnumFormat(GoodsVatRates)
}

object GoodsVatRates extends Enum[GoodsVatRate] {
  override val values: immutable.IndexedSeq[GoodsVatRate] = findValues

  case object Zero extends GoodsVatRate {
    override val value: Int = 0
  }

  case object Five extends GoodsVatRate {
    override val value: Int = 5
  }

  case object Twenty extends GoodsVatRate {
    override val value: Int = 20
  }

}


sealed trait GoodsDestination extends EnumEntry {
  def threshold: AmountInPence
}

object GoodsDestination {
  implicit val format: Format[GoodsDestination] = EnumFormat(GoodsDestinations)
}

object GoodsDestinations extends Enum[GoodsDestination] {
  override val values: immutable.IndexedSeq[GoodsDestination] = findValues

  case object NorthernIreland extends GoodsDestination {
    override val threshold: AmountInPence = AmountInPence(87300)
  }

  case object GreatBritain extends GoodsDestination {
    override val threshold: AmountInPence = AmountInPence(150000)
  }

}

import scala.collection.immutable

sealed trait DeclarationType extends EnumEntry

object DeclarationType extends Enum[DeclarationType] {
  override val values: immutable.IndexedSeq[DeclarationType] = findValues

  case object Import extends DeclarationType

  case object Export extends DeclarationType

}
