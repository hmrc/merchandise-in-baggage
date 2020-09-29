/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage

import java.time.LocalDate

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import uk.gov.hmrc.merchandiseinbaggage.config.CurrencyConversionConfiguration

trait CurrencyConversionStub extends BaseSpec with BaseSpecWithWireMock with CurrencyConversionConfiguration {

  private val todayDate: LocalDate = LocalDate.now

  def getCurrencyConversionStub(currency: String): StubMapping =
    currencyConversionMockServer.stubFor(get(urlEqualTo(s"${currencyConversion(todayDate, currency)}"))
      .willReturn(okJson(responseTemplate)))

  val responseTemplate =
    s"""[
       |    {
       |        "startDate": "$todayDate",
       |        "endDate": "$todayDate",
       |        "currencyCode": "USD",
       |        "rate": "1.3064"
       |    }
       |]""".stripMargin

}
