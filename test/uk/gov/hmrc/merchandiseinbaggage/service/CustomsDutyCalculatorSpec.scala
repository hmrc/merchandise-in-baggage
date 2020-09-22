/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import uk.gov.hmrc.merchandiseinbaggage.BaseSpec
import uk.gov.hmrc.merchandiseinbaggage.model.core.{AmountInPence, GBP}

class CustomsDutyCalculatorSpec extends BaseSpec {

  "will calculate a customs duty in pounds and pence" in new CustomsDutyCalculator {

    customDuty(GBP, AmountInPence(100.0)) mustBe AmountInPence(10.0)
  }
}
