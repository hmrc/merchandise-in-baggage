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
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResult, CalculationResults}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.mongo.ReactiveRepository

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class DeclarationUpdateRepository @Inject()(mongo: () => DB, repo: DeclarationRepository)(implicit ec: ExecutionContext)
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
        "dateOfDeclaration"                              -> Json.parse("""{"$gte": "2021-01-01T00:00:00.001"}"""),
        "declarationGoods.goods.categoryQuantityOfGoods" -> Json.parse("""{"$exists": true}""")
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
    log.warn(s"Starting transformation for declaration: $declarationId")
    val updated = transformJson(record, declarationId)
    log.warn(s"Successfully transformed declaration, declarationId: $declarationId")
    collection
      .update(ordered = false)
      .one(Json.obj("declarationId" -> declarationId), updated, upsert = true)
      .map { _ =>
        log.warn(s"Successfully upserted declaration, declarationId: $declarationId")
        updated
      }
  }

  private def transformJson(record: JsObject, declarationId: String): JsObject = {

    val declarationType = (record \ "declarationType").as[DeclarationType]

    val transformGoods =
      (__ \ "declarationGoods" \ "goods").json.update(of[JsArray].map(_ => JsArray(updateGoods(record, declarationType))))

    val transformTotalCalculationResult = (__ \ "maybeTotalCalculationResult").json.update(
      __.readNullable[JsObject].map {
        case Some(obj) => Json.toJson(transformCalculationResult(obj))
      }
    )

    val transformAmendments = (__ \ "amendments").json.update(
      __.readNullable[JsArray].map {
        case Some(JsArray(_)) => JsArray(updateAmendments(record, declarationType))
      }
    )

    var result = tryTransform(transformGoods, record, declarationId)

    result = declarationType match {
      case Import => tryTransform(transformTotalCalculationResult, result, declarationId)
      case Export => result
    }

    result = (record \ "amendments").asOpt[JsArray] match {
      case Some(arr) if arr.value.nonEmpty => tryTransform(transformAmendments, result, declarationId)
      case _                               => result
    }

    result
  }

  private def tryTransform(reads: Reads[JsObject], in: JsObject, declarationId: String) =
    Try {
      in.transform(reads) match {
        case JsSuccess(updated, _) => updated
        case JsError(errors) =>
          log.warn(s"ignoring the failed transformation, errors: $errors, declarationId: $declarationId")
          in
      }
    } match {
      case Success(value) => value
      case Failure(ex) =>
        log.warn(s"ignoring the failed transformation with exception: ${ex.getMessage}, declarationId: $declarationId")
        in
    }

  private def updateGoods(record: JsObject, declarationType: DeclarationType): IndexedSeq[JsValue] = declarationType match {
    case Import =>
      (record \ "declarationGoods" \ "goods").as[JsArray].value.map { json =>
        val category = (json \ "categoryQuantityOfGoods" \ "category").as[String]
        val goodsVatRate = (json \ "goodsVatRate").as[GoodsVatRate]
        val producedInEu = (json \ "producedInEu").as[YesNoDontKnow]
        val purchaseDetails = (json \ "purchaseDetails").as[PurchaseDetails]

        Json.toJson(ImportGoods(category, goodsVatRate, producedInEu, purchaseDetails))
      }

    case Export =>
      (record \ "declarationGoods" \ "goods").as[JsArray].value.map { json =>
        val category = (json \ "categoryQuantityOfGoods" \ "category").as[String]
        val country = (json \ "destination").as[Country]
        val purchaseDetails = (json \ "purchaseDetails").as[PurchaseDetails]

        Json.toJson(ExportGoods(category, country, purchaseDetails))
      }
  }

  private def updateAmendments(record: JsObject, declarationType: DeclarationType) =
    (record \ "amendments").as[JsArray].value.map { json =>
      val reference = (json \ "reference").as[Int]
      val dateOfAmendment = (json \ "dateOfAmendment").as[LocalDateTime]
      val goods = declarationType match {
        case Import =>
          (json \ "goods" \ "goods").as[JsArray].value.map { json =>
            val category = (json \ "categoryQuantityOfGoods" \ "category").as[String]
            val goodsVatRate = (json \ "goodsVatRate").as[GoodsVatRate]
            val producedInEu = (json \ "producedInEu").as[YesNoDontKnow]
            val purchaseDetails = (json \ "purchaseDetails").as[PurchaseDetails]

            ImportGoods(category, goodsVatRate, producedInEu, purchaseDetails)
          }

        case Export =>
          (json \ "goods" \ "goods").as[JsArray].value.map { json =>
            val category = (json \ "categoryQuantityOfGoods" \ "category").as[String]
            val country = (json \ "destination").as[Country]
            val purchaseDetails = (json \ "purchaseDetails").as[PurchaseDetails]

            ExportGoods(category, country, purchaseDetails)
          }
      }

      val maybeTotalCalculationResult = (json \ "maybeTotalCalculationResult").asOpt[JsObject] match {
        case Some(jsObject) => Some(transformCalculationResult(jsObject))
        case None           => None
      }

      val paymentStatus = (json \ "paymentStatus").asOpt[PaymentStatus]
      val source = (json \ "source").asOpt[String]
      val emailsSent = (json \ "emailsSent").as[Boolean]
      val lang = (json \ "lang").as[String]

      Json.toJson(
        Amendment(
          reference,
          dateOfAmendment,
          DeclarationGoods(goods),
          maybeTotalCalculationResult,
          paymentStatus,
          source,
          emailsSent,
          lang))
    }

  private def transformCalculationResult(obj: JsObject) = {

    val calculationResults = (obj \ "calculationResults" \ "calculationResults").as[JsArray].value.map { json =>
      val category = (json \ "goods" \ "categoryQuantityOfGoods" \ "category").as[String]
      val goodsVatRate = (json \ "goods" \ "goodsVatRate").as[GoodsVatRate]
      val producedInEu = (json \ "goods" \ "producedInEu").as[YesNoDontKnow]
      val purchaseDetails = (json \ "goods" \ "purchaseDetails").as[PurchaseDetails]

      val updatedGoods = ImportGoods(category, goodsVatRate, producedInEu, purchaseDetails)

      val gbpAmount = (json \ "gbpAmount").as[AmountInPence]
      val duty = (json \ "duty").as[AmountInPence]
      val vat = (json \ "vat").as[AmountInPence]
      val conversionRatePeriod = (json \ "conversionRatePeriod").asOpt[ConversionRatePeriod]

      CalculationResult(updatedGoods, gbpAmount, duty, vat, conversionRatePeriod)
    }

    val totalGbpValue = (obj \ "totalGbpValue").as[AmountInPence]
    val totalTaxDue = (obj \ "totalTaxDue").as[AmountInPence]
    val totalDutyDue = (obj \ "totalDutyDue").as[AmountInPence]
    val totalVatDue = (obj \ "totalVatDue").as[AmountInPence]

    TotalCalculationResult(CalculationResults(calculationResults), totalGbpValue, totalTaxDue, totalDutyDue, totalVatDue)
  }
}
