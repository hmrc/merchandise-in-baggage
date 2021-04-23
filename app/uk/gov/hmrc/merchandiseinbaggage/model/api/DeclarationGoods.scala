/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json._
import uk.gov.hmrc.merchandiseinbaggage.model.api.ExportGoods.categoryReads

sealed trait Goods {
  val category: String
  val purchaseDetails: PurchaseDetails
}

final case class ImportGoods(
  category: String,
  goodsVatRate: GoodsVatRate,
  producedInEu: YesNoDontKnow,
  purchaseDetails: PurchaseDetails
) extends Goods

object ImportGoods {
  //TODO: Custom reads until we cleanup production data MIBM-588
  implicit val format: OFormat[ImportGoods] = new OFormat[ImportGoods] {
    override def writes(o: ImportGoods): JsObject =
      Json.obj(
        "category"        -> o.category,
        "goodsVatRate"    -> o.goodsVatRate,
        "producedInEu"    -> o.producedInEu,
        "purchaseDetails" -> o.purchaseDetails,
      )

    override def reads(json: JsValue): JsResult[ImportGoods] = {
      val vatRate = (json \ "goodsVatRate").as[GoodsVatRate]
      val producedInEu = (json \ "producedInEu").as[YesNoDontKnow]
      val purchaseDetails = (json \ "purchaseDetails").as[PurchaseDetails]

      JsSuccess(ImportGoods(categoryReads(json), vatRate, producedInEu, purchaseDetails))
    }
  }
}

final case class ExportGoods(
  category: String,
  destination: Country,
  purchaseDetails: PurchaseDetails
) extends Goods

object ExportGoods {
  implicit val format: OFormat[ExportGoods] = new OFormat[ExportGoods] {
    override def writes(o: ExportGoods): JsObject =
      Json.obj(
        "category"        -> o.category,
        "destination"     -> o.destination,
        "purchaseDetails" -> o.purchaseDetails,
      )

    override def reads(json: JsValue): JsResult[ExportGoods] = {
      val destination = (json \ "destination").as[Country]
      val purchaseDetails = (json \ "purchaseDetails").as[PurchaseDetails]

      JsSuccess(ExportGoods(categoryReads(json), destination, purchaseDetails))
    }
  }

  def categoryReads(json: JsValue): String =
    (json \ "category").asOpt[String] match {
      case Some(category) => category
      case None           => (json \ "categoryQuantityOfGoods" \ "category").as[String]
    }
}

object Goods {
  implicit val writes = Writes[Goods] {
    case ig: ImportGoods => ImportGoods.format.writes(ig)
    case eg: ExportGoods => ExportGoods.format.writes(eg)
  }

  //TODO: Custom reads until we cleanup production data MIBM-588
  implicit val reads = Reads[Goods] {
    case json: JsObject if json.keys.contains("producedInEu") =>
      val vatRate = (json \ "goodsVatRate").as[GoodsVatRate]
      val producedInEu = (json \ "producedInEu").as[YesNoDontKnow]
      val purchaseDetails = (json \ "purchaseDetails").as[PurchaseDetails]

      JsSuccess(ImportGoods(categoryReads(json), vatRate, producedInEu, purchaseDetails))

    case json: JsObject if json.keys.contains("destination") =>
      val destination = (json \ "destination").as[Country]
      val purchaseDetails = (json \ "purchaseDetails").as[PurchaseDetails]

      JsSuccess(ExportGoods(categoryReads(json), destination, purchaseDetails))
  }

  private def categoryReads(json: JsObject): String =
    (json \ "category").asOpt[String] match {
      case Some(category) => category
      case None           => (json \ "categoryQuantityOfGoods" \ "category").as[String]
    }
}

case class DeclarationGoods(goods: Seq[Goods])

object DeclarationGoods {
  implicit val format: OFormat[DeclarationGoods] = Json.format[DeclarationGoods]
}
