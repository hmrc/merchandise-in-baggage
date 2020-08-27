/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model

import java.time.LocalDateTime

import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.BaseSpec

class DeclarationSpec extends BaseSpec {

  "Serialise/Deserialise from/to json to Declaration" in {
    val declaration = Declaration(
      DeclarationId("1234"),
      TraderName("Valentino Rossi"),
      Amount(111),
      CsgTpsProviderId("123"),
      ChargeReference("some ref"),
      Outstanding,
      Some(LocalDateTime.now),
      None
    )

    val actual = Json.toJson(declaration).toString

    Json.toJson(declaration) mustBe Json.parse(actual)
  }
}
