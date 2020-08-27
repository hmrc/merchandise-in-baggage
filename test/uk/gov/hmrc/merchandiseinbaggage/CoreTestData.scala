/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage

import java.time.LocalDateTime
import java.util.UUID

import uk.gov.hmrc.merchandiseinbaggage.model._

trait CoreTestData {

  def aDeclaration = Declaration(DeclarationId(UUID.randomUUID().toString),
    TraderName("name"), Amount(1), CsgTpsProviderId("123"), ChargeReference("ref"), Outstanding,
    Some(LocalDateTime.now), None)
}
