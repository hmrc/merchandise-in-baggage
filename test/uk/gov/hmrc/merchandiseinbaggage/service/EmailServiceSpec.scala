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

package uk.gov.hmrc.merchandiseinbaggage.service

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.EmailConnector
import uk.gov.hmrc.merchandiseinbaggage.model.DeclarationEmailInfo
import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationRepository
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EmailServiceSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures with MockFactory {

  val declarationRepo = mock[DeclarationRepository]
  val emailConnector = mock[EmailConnector]

  val emailService = new EmailService(emailConnector, declarationRepo)

  "sendEmails" when {
    "sending emails to new declarations" should {
      "send" in {}
    }
  }

  "sendEmails should handle New declarations" in {
    val declaration = aDeclaration
    val declarationWithEmailSent = declaration.copy(emailsSent = true)
    (emailConnector
      .sendEmails(_: DeclarationEmailInfo)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returns(Future.successful(202))
      .twice()

    (declarationRepo
      .upsertDeclaration(_: Declaration))
      .expects(declarationWithEmailSent)
      .returns(Future.successful(declarationWithEmailSent))

    emailService.sendEmails(declaration).value.futureValue mustBe Right(declarationWithEmailSent)
  }

  "sendEmails should handle Amended declarations" in {
    val declaration = aDeclarationWithAmendment
    val updatedAmendment = aAmendment.copy(emailsSent = true)
    val amendedDeclarationWithEmailSent = declaration.copy(amendments = Seq(updatedAmendment))
    (emailConnector
      .sendEmails(_: DeclarationEmailInfo)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returns(Future.successful(202))
      .twice()

    (declarationRepo
      .upsertDeclaration(_: Declaration))
      .expects(amendedDeclarationWithEmailSent)
      .returns(Future.successful(amendedDeclarationWithEmailSent))

    emailService.sendEmails(declaration, Some(1)).value.futureValue mustBe Right(amendedDeclarationWithEmailSent)
  }

  "should not send emails if they are already sent" in {
    val declaration = aDeclaration.copy(emailsSent = true)
    emailService.sendEmails(declaration).value.futureValue mustBe Right(declaration)
  }

}
