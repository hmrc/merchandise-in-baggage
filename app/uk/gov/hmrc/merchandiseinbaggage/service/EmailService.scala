/*
 * Copyright 2024 HM Revenue & Customs
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

import cats.data.EitherT
import cats.implicits._
import javax.inject.Inject
import play.api.Logging
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.mvc.Http.Status.ACCEPTED
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.EmailConnector
import uk.gov.hmrc.merchandiseinbaggage.model.DeclarationEmailInfo
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{BusinessError, EmailSentError}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.util.DateUtils._
import uk.gov.hmrc.merchandiseinbaggage.util.PagerDutyHelper
import uk.gov.hmrc.merchandiseinbaggage.util.Utils.FutureOps

import scala.concurrent.{ExecutionContext, Future}

class EmailService @Inject() (emailConnector: EmailConnector, declarationRepository: DeclarationRepository)(implicit
  val appConfig: AppConfig,
  messagesApi: MessagesApi,
  ec: ExecutionContext
) extends Logging {

  val messagesEN: Messages = MessagesImpl(Lang("en"), messagesApi)
  val messagesCY: Messages = MessagesImpl(Lang("cy"), messagesApi)

  def sendEmails(declaration: Declaration, amendmentReference: Option[Int] = None)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, BusinessError, Declaration] = {

    implicit val messages: Messages = if (declaration.lang == "en") messagesEN else messagesCY

    EitherT(
      if (emailsSent(declaration, amendmentReference)) {
        logger.warn(
          s"[EmailService][sendEmails] emails are already sent for declaration: ${declaration.mibReference.value}"
        )
        declaration.asRight.asFuture
      } else {
        val emailToBF     =
          emailConnector.sendEmails(toEmailInfo(declaration, appConfig.bfEmail, "BorderForce", amendmentReference))
        val emailToTrader = declaration.email match {
          case Some(email) =>
            emailConnector.sendEmails(toEmailInfo(declaration, email.email, "Trader", amendmentReference))
          case None        => ACCEPTED.asFuture
        }

        (emailToBF, emailToTrader).mapN { (bfResponse, trResponse) =>
          (bfResponse, trResponse) match {
            case (ACCEPTED, ACCEPTED) =>
              updateDeclarationWithEmailSent(declaration, amendmentReference).map(_.asRight)
            case (s1, s2)             =>
              val message = s"Error in sending emails, bfResponse:$s1, trResponse:$s2"
              PagerDutyHelper.alert(Some(message))
              EmailSentError(message).asLeft.asFuture
          }
        }.flatten
      }
    )
  }

  private def updateDeclarationWithEmailSent(
    declaration: Declaration,
    amendmentReference: Option[Int]
  ): Future[Declaration] = {
    val updatedDeclaration = amendmentReference match {
      case Some(reference) =>
        val updatedAmendments = declaration.amendments.map { amendment =>
          amendment.reference match {
            case r if r == reference => amendment.copy(emailsSent = true)
            case _                   => amendment
          }
        }
        declaration.copy(amendments = updatedAmendments)

      case None => declaration.copy(emailsSent = true)
    }

    declarationRepository.upsertDeclaration(updatedDeclaration)
  }

  private def emailsSent(declaration: Declaration, amendmentReference: Option[Int]): Boolean =
    amendmentReference match {
      case Some(reference) =>
        declaration.amendments.find(_.reference == reference) match {
          case Some(amendment) => amendment.emailsSent
          case None            => true // no amendment found for given amendmentReference, do not trigger emails
        }
      case None            =>
        // first check for latest `amend` journey, then fallback to `new` journey
        declaration.amendments.lastOption.map(_.emailsSent).getOrElse(declaration.emailsSent)
    }

  private def toEmailInfo(
    declaration: Declaration,
    emailTo: String,
    emailType: String,
    amendmentReference: Option[Int]
  )(implicit messages: Messages): DeclarationEmailInfo = {
    import declaration._

    val amendmentGoods =
      declarationType match {
        case Import =>
          amendments
            .filter(a => a.paymentStatus.contains(Paid) || a.paymentStatus.contains(NotRequired))
            .flatMap(_.goods.goods)
        case Export => amendments.flatMap(_.goods.goods)
      }

    val allGoods = declarationGoods.goods ++ amendmentGoods

    val goodsParams = allGoods.zipWithIndex
      .map { goodsWithIdx =>
        val (goods, idx) = goodsWithIdx
        goods match {
          case ig: ImportGoods =>
            Map(
              s"goodsCategory_$idx"     -> ig.category,
              s"goodsProducedInEu_$idx" -> messages(ig.producedInEu.messageKey),
              s"goodsPrice_$idx"        -> ig.purchaseDetails.formatted
            )
          case eg: ExportGoods =>
            Map(
              s"goodsCategory_$idx"    -> eg.category,
              s"goodsDestination_$idx" -> eg.destination.displayName,
              s"goodsPrice_$idx"       -> eg.purchaseDetails.formatted
            )
        }
      }
      .reduce(_ ++ _)

    val commonParams = Map(
      "emailTo"                   -> emailType,
      "nameOfPersonCarryingGoods" -> nameOfPersonCarryingTheGoods.toString,
      "surname"                   -> nameOfPersonCarryingTheGoods.lastName,
      "declarationReference"      -> mibReference.value,
      "dateOfDeclaration"         -> dateOfDeclaration.formattedDate,
      "eori"                      -> eori.value
    )

    val allParams =
      if (declarationType == DeclarationType.Import) {
        goodsParams ++ commonParams ++ paymentParams(declaration)
      } else {
        goodsParams ++ commonParams
      }

    val journeyType =
      if (amendments.isEmpty) {
        New
      } else {
        Amend
      }

    val lang = amendmentReference match {
      case Some(reference) =>
        declaration.amendments.find(_.reference == reference) match {
          case Some(amendment) => amendment.lang
          case None            => declaration.lang
        }
      case None            => declaration.lang
    }

    DeclarationEmailInfo(
      Seq(emailTo),
      templateId(lang, declarationType, journeyType),
      allParams
    )
  }

  private def templateId(lang: String, declarationType: DeclarationType, journeyType: JourneyType): String = {

    val templateId = (declarationType, journeyType) match {
      case (Import, New)   => "mods_import_declaration"
      case (Import, Amend) => "mods_amend_import_declaration"
      case (Export, New)   => "mods_export_declaration"
      case (Export, Amend) => "mods_amend_export_declaration"
    }

    if (lang == "en") {
      templateId
    } else {
      templateId + "_cy"
    }
  }

  private case class PaymentMade(totalDutyDue: AmountInPence, totalVatDue: AmountInPence, totalTaxDue: AmountInPence)

  private def paymentParams(declaration: Declaration) = {

    def paymentForCalculation(maybeTotalCalculationResult: Option[TotalCalculationResult]) =
      maybeTotalCalculationResult match {
        case Some(total) => PaymentMade(total.totalDutyDue, total.totalVatDue, total.totalTaxDue)
        case None        => PaymentMade(AmountInPence(0), AmountInPence(0), AmountInPence(0))
      }

    val declarationPayment = paymentForCalculation(declaration.maybeTotalCalculationResult)

    val amendmentPayments = declaration.amendments
      .filter(a => a.paymentStatus.contains(Paid) || a.paymentStatus.contains(NotRequired))
      .map(a => paymentForCalculation(a.maybeTotalCalculationResult))

    val totalPaymentsMade = declarationPayment +: amendmentPayments

    val totalCustomsDuty = AmountInPence(totalPaymentsMade.map(_.totalDutyDue.value).sum)
    val totalVat         = AmountInPence(totalPaymentsMade.map(_.totalVatDue.value).sum)
    val totalTax         = AmountInPence(totalPaymentsMade.map(_.totalTaxDue.value).sum)

    Map(
      "customsDuty" -> totalCustomsDuty.formattedInPounds,
      "vat"         -> totalVat.formattedInPounds,
      "total"       -> totalTax.formattedInPounds
    )
  }
}
