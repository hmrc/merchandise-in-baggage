/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.config

import pureconfig.ConfigSource

object AppConfigSource {
  val configSource: String => ConfigSource = ConfigSource.default.at
}
