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
import play.api.libs.json._
import reactivemongo.api.Cursor.FailOnError
import reactivemongo.api.{DB, ReadPreference}
import reactivemongo.play.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationRequest, CalculationResults}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class DeclarationUpdateRepository @Inject()(mongo: () => DB, calculationService: CalculationService, repo: DeclarationRepository)(
  implicit ec: ExecutionContext)
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
        "declarationType"             -> "Import",
        "dateOfDeclaration"           -> Json.parse("""{"$gte": "2021-01-01T00:00:00.001"}"""),
        "maybeTotalCalculationResult" -> Json.parse("""{"$exists": false}""")
      )

    collection
      .find(query)
      .cursor[JsObject](ReadPreference.primaryPreferred)
      .collect(maxDocs = 5, FailOnError[List[JsObject]]())
      .map { list =>
        list.foreach { record =>
          record.asOpt[Declaration] match {
            case Some(declaration) =>
              Try {
                transformDeclaration(declaration)
              } match {
                case Success(value) => value
                case Failure(ex) =>
                  log.warn(s"Failed to trasnform the declaration, error: ${ex.getMessage}")
              }

            case _ =>
              val declarationId = (record \ "declarationId").as[String]
              log.warn(s"Failed to deserialize for declarationId: $declarationId")
          }
        }
      }
  }

  private def transformDeclaration(declaration: Declaration): Future[Unit] = {
    log.warn(s"Starting transformation for declarationId: ${declaration.declarationId}")

    implicit val hc: HeaderCarrier = HeaderCarrier()

    val calculationRequests =
      declaration.declarationGoods.goods
        .collect { case goods: ImportGoods => goods }
        .map(CalculationRequest(_))

    val result = Future
      .traverse(calculationRequests) { request =>
        calculationService.calculate(request, declaration.dateOfDeclaration.toLocalDate)
      }
      .map(CalculationResults(_))
      .map { calculationResults =>
        TotalCalculationResult(
          calculationResults,
          AmountInPence(calculationResults.calculationResults.map(_.gbpAmount.value).sum),
          AmountInPence(calculationResults.calculationResults.map(_.taxDue.value).sum),
          AmountInPence(calculationResults.calculationResults.map(_.duty.value).sum),
          AmountInPence(calculationResults.calculationResults.map(_.vat.value).sum)
        )
      }

    result.flatMap { res =>
      val updated = declaration.copy(maybeTotalCalculationResult = Some(res))

      repo.upsertDeclaration(updated).map { _ =>
        log.warn(s"Successfully upserted declarationId: ${declaration.declarationId}")
      }
    }
  }

}
