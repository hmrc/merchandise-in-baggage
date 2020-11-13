package uk.gov.hmrc.merchandiseinbaggage.service

import java.time.LocalDateTime

import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationId
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}

class DeclarationDateOrderingSpec extends BaseSpec with CoreTestData {

  "find latest of a list declaration created date" in new DeclarationDateOrdering {
    val declaration = aDeclaration
    val newest = 20
    val now = LocalDateTime.now
    val declarations: List[Declaration] = (1 to newest).toList.map(idx =>
      declaration.copy(declarationId = DeclarationId(idx.toString)).copy(dateOfDeclaration = now.plusMinutes(idx))
    )

    latest(declarations).dateOfDeclaration.withSecond(0) mustBe now.plusMinutes(newest).withSecond(0)
  }
}
