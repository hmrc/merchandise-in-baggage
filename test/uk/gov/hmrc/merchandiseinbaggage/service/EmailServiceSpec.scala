/*
 * Copyright 2025 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.ACCEPTED
import uk.gov.hmrc.merchandiseinbaggage.connectors.EmailConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Export
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationGoods, Paid}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.Future

class EmailServiceSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures with OptionValues {

  val declarationRepo: DeclarationRepository = mock(classOf[DeclarationRepository])
  val emailConnector: EmailConnector         = mock(classOf[EmailConnector])

  val emailService = new EmailService(emailConnector, declarationRepo)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(declarationRepo)
    reset(emailConnector)
  }

  "sendEmails should handle New Import declarations" in {
    val declaration              = aDeclaration
    val declarationWithEmailSent = declaration.copy(emailsSent = true)

    when(emailConnector.sendEmails(any())(any(), any())).thenReturn(Future.successful(ACCEPTED))

    when(declarationRepo.upsertDeclaration(any()))
      .thenReturn(Future.successful(declarationWithEmailSent))

    emailService.sendEmails(declaration).value.futureValue mustBe Right(declarationWithEmailSent)

    verify(emailConnector, times(2)).sendEmails(any())(any(), any())
    verify(declarationRepo).upsertDeclaration(declarationWithEmailSent)
  }

  "sendEmails should handle New Export declarations" in {
    val declaration =
      aDeclaration.copy(declarationType = Export, declarationGoods = DeclarationGoods(Seq(aExportGoods)))

    val declarationWithEmailSent = declaration.copy(emailsSent = true)
    when(emailConnector.sendEmails(any())(any(), any())).thenReturn(Future.successful(ACCEPTED))

    when(declarationRepo.upsertDeclaration(any()))
      .thenReturn(Future.successful(declarationWithEmailSent))

    emailService.sendEmails(declaration).value.futureValue mustBe Right(declarationWithEmailSent)

    verify(emailConnector, times(2)).sendEmails(any())(any(), any())
    verify(declarationRepo).upsertDeclaration(declarationWithEmailSent)
  }

  "sendEmails should handle Amended Import declarations" in {
    val declaration                     = aDeclarationWithAmendment
    val updatedAmendment                = aAmendment.copy(emailsSent = true)
    val amendedDeclarationWithEmailSent = declaration.copy(amendments = Seq(updatedAmendment))
    when(emailConnector.sendEmails(any())(any(), any())).thenReturn(Future.successful(ACCEPTED))

    when(declarationRepo.upsertDeclaration(any()))
      .thenReturn(Future.successful(amendedDeclarationWithEmailSent))

    emailService.sendEmails(declaration, Some(1)).value.futureValue mustBe Right(amendedDeclarationWithEmailSent)

    verify(emailConnector, times(2)).sendEmails(any())(any(), any())
    verify(declarationRepo).upsertDeclaration(amendedDeclarationWithEmailSent)
  }

  "sendEmails should consider only paid amendments" in {
    val paidAmendment                   = aAmendment.copy(paymentStatus = Some(Paid))
    val declaration                     = aDeclaration.copy(amendments = Seq(paidAmendment))
    val updatedAmendment                = paidAmendment.copy(emailsSent = true)
    val amendedDeclarationWithEmailSent = declaration.copy(amendments = Seq(updatedAmendment))
    when(emailConnector.sendEmails(any())(any(), any())).thenReturn(Future.successful(ACCEPTED))

    when(declarationRepo.upsertDeclaration(any()))
      .thenReturn(Future.successful(amendedDeclarationWithEmailSent))

    emailService.sendEmails(declaration, Some(1)).value.futureValue mustBe Right(amendedDeclarationWithEmailSent)

    verify(emailConnector, times(2)).sendEmails(any())(any(), any())
    verify(declarationRepo).upsertDeclaration(amendedDeclarationWithEmailSent)
  }

  "sendEmails should handle Amended Export declarations" in {
    val declaration                     = aDeclarationWithAmendment.copy(declarationType = Export)
    val updatedAmendment                = aAmendment.copy(emailsSent = true)
    val amendedDeclarationWithEmailSent = declaration.copy(amendments = Seq(updatedAmendment))
    when(emailConnector.sendEmails(any())(any(), any())).thenReturn(Future.successful(ACCEPTED))

    when(declarationRepo.upsertDeclaration(any()))
      .thenReturn(Future.successful(amendedDeclarationWithEmailSent))

    emailService.sendEmails(declaration, Some(1)).value.futureValue mustBe Right(amendedDeclarationWithEmailSent)

    verify(emailConnector, times(2)).sendEmails(any())(any(), any())
    verify(declarationRepo).upsertDeclaration(amendedDeclarationWithEmailSent)
  }

  "sendEmails should use the correct language" in {
    val amendment                       = aAmendment.copy(lang = "cy")
    val declaration                     = aDeclaration.copy(declarationType = Export, amendments = Seq(amendment))
    val updatedAmendment                = amendment.copy(emailsSent = true)
    val amendedDeclarationWithEmailSent = declaration.copy(amendments = Seq(updatedAmendment))
    when(emailConnector.sendEmails(any())(any(), any())).thenReturn(Future.successful(ACCEPTED))

    when(declarationRepo.upsertDeclaration(any()))
      .thenReturn(Future.successful(amendedDeclarationWithEmailSent))

    emailService.sendEmails(declaration, Some(1)).value.futureValue mustBe Right(amendedDeclarationWithEmailSent)

    verify(emailConnector, times(2)).sendEmails(any())(any(), any())
    verify(declarationRepo).upsertDeclaration(amendedDeclarationWithEmailSent)
  }

  "should not send emails if they are already sent" in {
    val declaration = aDeclaration.copy(emailsSent = true)
    emailService.sendEmails(declaration).value.futureValue mustBe Right(declaration)
  }

}
