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

package uk.gov.hmrc.merchandiseinbaggage.service

import cats.data.EitherT
import cats.implicits._
import com.google.inject.Inject
import play.api.Logging
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.mvc.Http.Status.ACCEPTED
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.EmailConnector
import uk.gov.hmrc.merchandiseinbaggage.model.DeclarationEmailInfo
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationType}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{BusinessError, EmailSentError}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.util.PagerDutyHelper
import uk.gov.hmrc.merchandiseinbaggage.util.DateUtils._

import scala.concurrent.{ExecutionContext, Future}

class EmailService @Inject()(emailConnector: EmailConnector, declarationRepository: DeclarationRepository)(
  implicit val appConfig: AppConfig,
  messagesApi: MessagesApi,
  ec: ExecutionContext)
    extends Logging {

  val messagesEN: Messages = MessagesImpl(Lang("en"), messagesApi)
  val messagesCY: Messages = MessagesImpl(Lang("cy"), messagesApi)

  def sendEmails(declaration: Declaration)(implicit hc: HeaderCarrier): EitherT[Future, BusinessError, Unit] = {
    implicit val messages: Messages = if (declaration.lang == "en") messagesEN else messagesCY

    val result: Future[Either[BusinessError, Unit]] = {
      if (declaration.emailsSent) {
        logger.warn(s"emails are already sent for declaration: ${declaration.mibReference.value}")
        Future.successful(Right(()))
      } else {
        val emailToBF = emailConnector.sendEmails(toEmailInfo(declaration, appConfig.bfEmail, "BorderForce"))
        val emailToTrader = emailConnector.sendEmails(toEmailInfo(declaration, declaration.email.email, "Trader"))

        (emailToBF, emailToTrader).mapN { (bfResponse, trResponse) =>
          (bfResponse, trResponse) match {
            case (ACCEPTED, ACCEPTED) =>
              declarationRepository
                .upsertDeclaration(declaration.copy(emailsSent = true))
                .map(_ => Right(()))
            case (s1, s2) =>
              val message = s"Error in sending emails, bfResponse:$s1, trResponse:$s2"
              PagerDutyHelper.alert(Some(message))
              Future.successful(Left(EmailSentError(message)))
          }
        }.flatten
      }
    }

    EitherT(result)
  }

  private def toEmailInfo(declaration: Declaration, emailTo: String, emailType: String)(
    implicit messages: Messages): DeclarationEmailInfo = {
    import declaration._

    val goodsParams = declarationGoods.goods.zipWithIndex
      .map { goodsWithIdx =>
        val (goods, idx) = goodsWithIdx
        val countryOrDestKey = if (declarationType == DeclarationType.Import) s"goodsCountry_$idx" else s"goodsDestination_$idx"
        Map(
          s"goodsCategory_$idx" -> goods.categoryQuantityOfGoods.category,
          s"goodsQuantity_$idx" -> goods.categoryQuantityOfGoods.quantity,
          countryOrDestKey      -> goods.countryOfPurchase.displayName,
          s"goodsPrice_$idx"    -> goods.purchaseDetails.formatted,
        )
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

    val calculationParams = {
      maybeTotalCalculationResult match {
        case Some(total) =>
          Map(
            "customsDuty" -> total.totalDutyDue.formattedInPounds,
            "vat"         -> total.totalVatDue.formattedInPounds,
            "total"       -> total.totalTaxDue.formattedInPounds
          )

        case None => Map.empty
      }
    }

    val allParams =
      if (declarationType == DeclarationType.Import)
        goodsParams ++ commonParams ++ calculationParams
      else
        goodsParams ++ commonParams

    DeclarationEmailInfo(
      Seq(emailTo),
      templateId(lang, declarationType),
      allParams
    )
  }

  private def templateId(lang: String, declarationType: DeclarationType): String = {
    val importTemplate = if (lang == "en") "mods_import_declaration" else "mods_import_declaration_cy"
    val exportTemplate = if (lang == "en") "mods_export_declaration" else "mods_export_declaration_cy"
    if (declarationType == DeclarationType.Import)
      importTemplate
    else
      exportTemplate
  }
}
