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

import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationId
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class DeclarationDateOrderingSpec extends BaseSpec with CoreTestData {

  "find latest of a list declaration created date" in new DeclarationDateOrdering {
    private val declaration  = aDeclaration
    private val newest       = 20
    private val now          = LocalDateTime.now.truncatedTo(ChronoUnit.MILLIS)
    private val declarations = (1 to newest).toList.map(idx =>
      declaration.copy(declarationId = DeclarationId(idx.toString)).copy(dateOfDeclaration = now.plusMinutes(idx))
    )

    latest(declarations).dateOfDeclaration.withSecond(0) mustBe now.plusMinutes(newest).withSecond(0)
  }
}
