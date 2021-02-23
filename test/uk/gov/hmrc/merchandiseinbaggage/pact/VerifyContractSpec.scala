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

package uk.gov.hmrc.merchandiseinbaggage.pact

import java.io.File

import com.itv.scalapact.shared.ProviderStateResult
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData

import scala.concurrent.duration._

class VerifyContractSpec extends PactVerifySuite with CoreTestData {

  private val pactDir = "../merchandise-in-baggage-frontend/pact/"
  private val contracts = new File(pactDir).listFiles()
  val frontendContract = contracts.find(_.getAbsolutePath.contains("merchandise-in-baggage-frontend_merchandise-in-baggage")).get

  "Verifying Consumer Contracts" must {
    "be able to verify it's contracts" in {
      verifyPact
        .withPactSource(
          loadFromLocal(frontendContract.getAbsolutePath)
        )
        .setupProviderState("given") {
          case "persistDeclarationTest" =>
            ProviderStateResult(true, req => req)
        }
        .runVerificationAgainst("localhost", testServerPort, 10.seconds)
    }
  }

  override def beforeEach(): Unit = super.beforeEach()
}
