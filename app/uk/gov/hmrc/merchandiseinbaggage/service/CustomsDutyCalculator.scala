/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import uk.gov.hmrc.merchandiseinbaggage.model.core.{AmountInPence, Currency}
import uk.gov.hmrc.merchandiseinbaggage.repositories.CustomsRate._

trait CustomsDutyCalculator {

  //TODO currency not used yet - waiting for conversion integration
  def customDuty(currency: Currency, amount: AmountInPence): AmountInPence =
    AmountInPence(amount.value * customFlatRate)
}
