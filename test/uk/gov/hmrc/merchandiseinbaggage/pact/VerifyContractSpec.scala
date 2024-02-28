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

package uk.gov.hmrc.merchandiseinbaggage.pact

import java.io.File

import com.itv.scalapact.shared.ProviderStateResult
import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationGoods, DeclarationId, Eori}
import uk.gov.hmrc.merchandiseinbaggage.stubs.{CurrencyConversionStub, EoriCheckStub}

import scala.concurrent.duration._

class VerifyContractSpec extends PactVerifySuite with CoreTestData {

  private val pactDir = scala.util.Properties.envOrElse("PACTTEST", "../merchandise-in-baggage-frontend/pact/")

  println("Running pacttest for: " + pactDir)

  private val contracts      = new File(pactDir).listFiles()
  val frontendContract: File = contracts
    .find(_.getAbsolutePath.contains(pactDir))
    .getOrElse(fail("contracts not found"))

  println("Running pacttest found: " + frontendContract.getAbsolutePath)

  "Verifying Consumer Contracts" must {
    "be able to verify it's contracts" in {
      verifyPact
        .withPactSource(
          loadFromLocal(frontendContract.getAbsolutePath)
        )
        .setupProviderState("given") {
          case "persistDeclarationTest" =>
            ProviderStateResult(true, req => req)

          case "amendDeclarationTest" =>
            ProviderStateResult(true, req => req)

          case state: String if state.split("XXX").head == "id1234" =>
            val declarationString = state.split("XXX").toList.drop(1).mkString
            val declaration       = Json.parse(declarationString).as[Declaration]
            repository.insertDeclaration(declaration).futureValue
            ProviderStateResult(true, req => req)

          case "calculatePaymentsTest" =>
            CurrencyConversionStub.givenCurrencyConversion()
            ProviderStateResult(true, req => req)

          case state @ "id789" =>
            CurrencyConversionStub.givenCurrencyConversion()
            repository
              .insertDeclaration(
                aDeclaration.copy(declarationId = DeclarationId(state), declarationGoods = DeclarationGoods(Seq.empty))
              )
              .futureValue
            ProviderStateResult(true, req => req)

          case "checkEoriNumberTest" =>
            EoriCheckStub.givenEoriCheck(Eori("GB123"), List(CheckResponse("GB123", true, None)))
            ProviderStateResult(true, req => req)

          case state: String if state.split("XXX").head == "findByTest" =>
            val declarationString = state.split("XXX")(1)
            val declaration       = Json.parse(declarationString).as[Declaration]
            repository.insertDeclaration(declaration).futureValue
            ProviderStateResult(true, req => req)

        }
        .runVerificationAgainst("localhost", testServerPort, 10.seconds)
    }
  }

  override def beforeEach(): Unit =
    super.beforeEach()
  repository.deleteAll()
}
