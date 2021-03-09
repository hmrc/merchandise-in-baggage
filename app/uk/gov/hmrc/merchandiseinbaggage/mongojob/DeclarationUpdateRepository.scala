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
import play.api.Logger
import play.api.libs.json.Reads.of
import play.api.libs.json._
import reactivemongo.api.Cursor.FailOnError
import reactivemongo.api.{DB, ReadPreference}
import reactivemongo.play.json._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class DeclarationUpdateRepository @Inject()(mongo: () => DB)(implicit ec: ExecutionContext)
    extends ReactiveRepository[JsObject, String](
      "declaration",
      mongo,
      domainFormat = implicitly[Format[JsObject]],
      idFormat = implicitly[Format[String]]) {

  private val log = Logger(getClass)

  def transformDeclarations(): Future[Unit] = {
    log.warn("inside transformDeclarations")
    val query =
      Json.obj(
        "source"            -> Json.parse("""{"$exists": false}"""),
        "declarationType"   -> Json.parse("""{"$exists": true}"""),
        "dateOfDeclaration" -> Json.parse("""{"$gte": "2021-01-01T00:00:00.001"}""")
      )

    collection
      .find(query)
      .cursor[JsObject](ReadPreference.primaryPreferred)
      .collect(maxDocs = 10, FailOnError[List[JsObject]]())
      .map { list =>
        list.foreach { record =>
          transformDeclaration(record, (record \ "declarationId").as[String])
        }
      }
  }

  private def transformDeclaration(record: JsObject, declarationId: String) = {
    log.warn(s"Starting transformation for declarationId: $declarationId")
    val updated = transformJson(record)
    log.warn(s"Successfully transformed declaration, declarationId: $declarationId")
    collection
      .update(ordered = false)
      .one(Json.obj("declarationId" -> declarationId), updated, upsert = true)
      .map { _ =>
        log.warn(s"Successfully upserted declaration, declarationId: $declarationId")
        updated
      }
  }

  private def transformJson(in: JsObject): JsObject = {

    val journeyType = (__ \ "journeyDetails" \ "_type").json.prune
    val email = (__ \ "email" \ "confirmation").json.prune

    var result = tryTransform(journeyType, in)
    result = tryTransform(email, result)

    val removeTotalCalculationResult = (__ \ "maybeTotalCalculationResult").json.prune
    result = tryTransform(removeTotalCalculationResult, result)

    val declarationType = (result \ "declarationType").as[DeclarationType]
    val paymentSuccess = (result \ "paymentSuccess").asOpt[Boolean]

    val paymentStatus: Option[PaymentStatus] = (declarationType, paymentSuccess) match {
      case (Import, Some(true))  => Some(Paid)
      case (Import, Some(false)) => None
      case (Import, None)        => None
      case (Export, _)           => Some(NotRequired)
    }

    val updatePaymentStatus = {
      __.json.update((__ \ "paymentStatus").json.put(Json.toJson(paymentStatus)))
    }
    result = tryTransform(updatePaymentStatus, result)

    val paymentSuccessPrune = (__ \ "paymentSuccess").json.prune
    result = tryTransform(paymentSuccessPrune, result)

    val setSource = {
      __.json.update((__ \ "source").json.put(JsString("Digital")))
    }

    result = tryTransform(setSource, result)

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
    result = tryTransform(updateGoods, result)
    result
  }

  private def tryTransform(reads: Reads[JsObject], in: JsObject) =
    Try {
      in.transform(reads) match {
        case JsSuccess(updated, _) => updated
        case JsError(errors) =>
          log.warn(s"ignoring the failed transformation, errors: $errors")
          in
      }
    } match {
      case Success(value) => value
      case Failure(ex) =>
        log.warn(s"ignoring the failed transformation with exception: ${ex.getMessage}")
        in
    }
}
