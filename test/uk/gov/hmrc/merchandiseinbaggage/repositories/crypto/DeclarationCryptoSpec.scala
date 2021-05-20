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

package uk.gov.hmrc.merchandiseinbaggage.repositories.crypto

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers._
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

class DeclarationCryptoSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures with BeforeAndAfterEach {

  private val declarationCrypto = injector.instanceOf[DeclarationCrypto]
  private val declaration = aDeclaration

  "encrypt -> decrypt must match the original" in {
    declarationCrypto.decryptDeclaration(declarationCrypto.encryptDeclaration(declaration)) mustBe declaration
  }

  "encrypt name should be different from original" in {
    declarationCrypto.encryptDeclaration(declaration).nameOfPersonCarryingTheGoods should not be declaration.nameOfPersonCarryingTheGoods
  }

  "encrypt email should be different from original" in {
    declarationCrypto.encryptDeclaration(declaration).email should not be declaration.email
  }

  "encrypt eori should be different from original" in {
    declarationCrypto.encryptDeclaration(declaration).eori should not be declaration.eori
  }

  "encrypt maybeCustomsAgent should be different from original" in {
    val declarationWithCustomsAgent = declaration.copy(maybeCustomsAgent = Some(aCustomsAgent))

    declarationCrypto
      .encryptDeclaration(declarationWithCustomsAgent)
      .maybeCustomsAgent should not be declarationWithCustomsAgent.maybeCustomsAgent
  }

  "encrypt journeyDetails should be different from original" in {
    val declarationWithJourneyDetails = declaration.copy(journeyDetails = aJourneyInASmallVehicle)

    declarationCrypto
      .encryptDeclaration(declarationWithJourneyDetails)
      .journeyDetails should not be declarationWithJourneyDetails.journeyDetails
  }

}
