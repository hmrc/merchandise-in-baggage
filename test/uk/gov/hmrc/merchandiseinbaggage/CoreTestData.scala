/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage

import java.util.UUID

import uk.gov.hmrc.merchandiseinbaggage.model._

trait CoreTestData {

  def aDeclaration = Declaration(UUID.randomUUID().toString,
    TraderName("name"), Amount(1), CsgTpsProviderId("123"), ChargeReference("ref"))
}
