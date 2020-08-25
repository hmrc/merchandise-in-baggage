/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage

import java.time.LocalDateTime
import java.util.UUID

import uk.gov.hmrc.merchandiseinbaggage.model.{Amount, Declaration, Name, Reference}

trait CoreTestData {

  val aDeclaration = Declaration(UUID.randomUUID().toString, Name("name"), Amount(1), Reference("ref"), LocalDateTime.now)
}
