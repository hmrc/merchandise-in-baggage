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

package uk.gov.hmrc.merchandiseinbaggage.mongojob

import com.google.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json.Reads.of
import play.api.libs.json._
import reactivemongo.api.Cursor.FailOnError
import reactivemongo.api.{DB, ReadPreference}
import uk.gov.hmrc.mongo.ReactiveRepository
import reactivemongo.play.json._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{CategoryQuantityOfGoods, Country, DeclarationType, ExportGoods, GoodsVatRate, ImportGoods, NotRequired, Paid, PaymentStatus, PurchaseDetails, YesNoDontKnow}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationUpdateRepository @Inject()(mongo: () => DB)(implicit ec: ExecutionContext)
    extends ReactiveRepository[JsObject, String](
      "declaration",
      mongo,
      domainFormat = implicitly[Format[JsObject]],
      idFormat = implicitly[Format[String]]) {

  def transformDeclarations() = {
    val query = Json.obj("source" -> Json.parse("""{"$exists": false}"""))
    collection
      .find(query)
      .cursor[JsObject](ReadPreference.primaryPreferred)
      .collect(maxDocs = 10, FailOnError[List[JsObject]]())
      .map { list =>
        list.map { record =>
          val declarationId = (record \ "declarationId").as[String]
          record.transform(transformJson(record)) match {
            case JsSuccess(updated, _) =>
              collection
                .update(ordered = false)
                .one(Json.obj("declarationId" -> declarationId), updated, upsert = true)
                .map { _ =>
                  logger.warn(s"Successfully transformed declaration, declarationId: $declarationId")
                  updated
                }
            case JsError(errors) =>
              logger.warn(s"Failed to transform declaration, declarationId: $declarationId errors: $errors")
              record
          }
        }
      }
  }

  private def transformJson(in: JsObject): Reads[JsObject] = {
    val typeAndEmail =
      (__ \ "journeyDetails" \ "_type").json.prune andThen (__ \ "email" \ "confirmation").json.prune

    val removeTotalCalculationResult =
      (__ \ "maybeTotalCalculationResult").json.prune

    val declarationType = (in \ "declarationType").as[DeclarationType]
    val paymentSuccess = (in \ "paymentSuccess").asOpt[Boolean]

    val paymentStatus: Option[PaymentStatus] = (declarationType, paymentSuccess) match {
      case (Import, Some(true))  => Some(Paid)
      case (Import, Some(false)) => None
      case (Import, None)        => None
      case (Export, _)           => Some(NotRequired)
    }

    val updatePaymentStatus = {
      __.json.update((__ \ "paymentStatus").json.put(Json.toJson(paymentStatus))) andThen
        (__ \ "paymentSuccess").json.prune
    }

    val setSource = {
      __.json.update((__ \ "source").json.put(JsString("Digital")))
    }

    val goods: IndexedSeq[JsValue] = declarationType match {
      case Import =>
        (in \ "declarationGoods" \ "goods").as[JsArray].value.map { json =>
          val categoryQuantity = (json \ "categoryQuantityOfGoods").as[CategoryQuantityOfGoods]
          val goodsVatRate = (json \ "goodsVatRate").as[GoodsVatRate]
          val country = (json \ "countryOfPurchase").as[Country]
          val purchaseDetails = (json \ "purchaseDetails").as[PurchaseDetails]

          val producedInEu = if (country.isEu) YesNoDontKnow.Yes else YesNoDontKnow.No

          Json.toJson(ImportGoods(categoryQuantity, goodsVatRate, producedInEu, purchaseDetails))
        }
      case Export =>
        (in \ "declarationGoods" \ "goods").as[JsArray].value.map { json =>
          val categoryQuantity = (json \ "categoryQuantityOfGoods").as[CategoryQuantityOfGoods]
          val country = (json \ "countryOfPurchase").as[Country]
          val purchaseDetails = (json \ "purchaseDetails").as[PurchaseDetails]

          Json.toJson(ExportGoods(categoryQuantity, country, purchaseDetails))
        }
    }

    val updateGoods = (__ \ "declarationGoods" \ "goods").json.update(of[JsArray].map(_ => JsArray(goods)))

    typeAndEmail andThen removeTotalCalculationResult andThen updatePaymentStatus andThen setSource andThen updateGoods
  }

}
