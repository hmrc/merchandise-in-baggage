/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.service

import play.api.Logger
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, Declaration}
import uk.gov.hmrc.merchandiseinbaggage.model.audit.RefundableDeclaration
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait Auditor {
  val auditConnector: AuditConnector
  val messagesApi: MessagesApi

  val messagesEN: Messages = MessagesImpl(Lang("en"), messagesApi)

  private val logger = Logger(this.getClass)

  def auditDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val eventType = if (declaration.amendments.isEmpty) "DeclarationComplete" else "DeclarationAmended"
    auditConnector
      .sendExtendedEvent(ExtendedDataEvent(auditSource = "merchandise-in-baggage", auditType = eventType, detail = toJson(declaration)))
      .recover {
        case NonFatal(e) => Failure(e.getMessage)
      }
      .map { status =>
        status match {
          case Success =>
            logger.info(s"Successful audit of declaration with id [${declaration.declarationId}]")
          case Disabled =>
            logger.warn(s"Audit of declaration with id [${declaration.declarationId}] returned Disabled")
          case Failure(message, _) =>
            logger.error(s"Audit of declaration with id [${declaration.declarationId}] returned Failure with message [$message]")
        }
        ()
      }
  }

  def auditRefundableDeclaration(declaration: Declaration, amendment: Option[Amendment] = None)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[Unit] = {
    val refundableDeclarations: Option[Seq[RefundableDeclaration]] = {
      val maybeCalculationResults =
        amendment
          .flatMap(_.maybeTotalCalculationResult.map(_.calculationResults.calculationResults))
          .orElse(declaration.maybeTotalCalculationResult.map(_.calculationResults.calculationResults))

      maybeCalculationResults.map { calculationResults =>
        calculationResults.map { calculationResult =>
          RefundableDeclaration(
            declaration.mibReference,
            declaration.nameOfPersonCarryingTheGoods.toString,
            declaration.eori.toString,
            calculationResult.goods.category,
            calculationResult.gbpAmount.formattedInPounds,
            calculationResult.duty.formattedInPounds,
            calculationResult.vat.formattedInPounds,
            s"${calculationResult.goods.goodsVatRate.value}%",
            calculationResult.taxDue.formattedInPounds,
            calculationResult.goods.producedInEu.entryName,
            calculationResult.goods.purchaseDetails.amount,
            calculationResult.goods.purchaseDetails.currency.code,
            calculationResult.conversionRatePeriod.fold("1.00")(_.rate.toString)
          )
        }
      }
    }

    refundableDeclarations.fold(Seq(Future.successful(()))) { declarations =>
      declarations.map { refund =>
        auditConnector
          .sendExtendedEvent(
            ExtendedDataEvent(auditSource = "merchandise-in-baggage", auditType = "RefundableDeclaration", detail = toJson(refund))
          )
          .recover {
            case NonFatal(e) => Failure(e.getMessage)
          }
          .map {
            case Success =>
              logger.info(s"Successful audit of declaration with id [${declaration.declarationId}]")
            case Disabled =>
              logger.warn(s"Audit of declaration with id [${declaration.declarationId}] returned Disabled")
            case Failure(message, _) =>
              logger.error(s"Audit of declaration with id [${declaration.declarationId}] returned Failure with message [$message]")
          }
      }
    }

    Future.successful(())
  }
}
