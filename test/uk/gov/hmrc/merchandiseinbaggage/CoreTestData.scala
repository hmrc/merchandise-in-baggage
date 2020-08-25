/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage

import uk.gov.hmrc.merchandiseinbaggage.model.{Amount, Declaration, Name, Reference}

trait CoreTestData {

  val aDeclaration = Declaration(Name("name"), Amount(1), Reference("ref"))
}
