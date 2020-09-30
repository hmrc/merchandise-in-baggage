/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.core

import play.api.libs.json.Format
import uk.gov.hmrc.merchandiseinbaggage.util.ValueClassFormat

case class Amount(value: Double)

object Amount {
  implicit val format: Format[Amount] = ValueClassFormat.formatDouble(value => Amount.apply(value))(_.value)
}