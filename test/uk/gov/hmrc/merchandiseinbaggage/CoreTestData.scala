/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage

import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.core._

trait CoreTestData {
  private val aTraderName = TraderName("name")
  private val anAmount = AmountInPence(1)
  private val aCsgTpsProviderId = CsgTpsProviderId("123")
  private val aChargeReference = ChargeReference("ref")

  val aDeclaration: Declaration = Declaration(aTraderName, anAmount, aCsgTpsProviderId, aChargeReference)

  val aDeclarationRequest: DeclarationRequest = DeclarationRequest(aTraderName, anAmount, aCsgTpsProviderId, aChargeReference)
}
