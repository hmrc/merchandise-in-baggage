/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage

import java.time.LocalDate

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping

trait CurrencyConversionStub extends BaseSpec with BaseSpecWithWireMock {

  private val todayDate: String = LocalDate.now.toString
  val currencyConverterEndpoint = s"/currency-conversion/rates/$todayDate"

  def getCurrencyConversionStub(currency: String): StubMapping =
    currencyConversionMockServer.stubFor(get(urlEqualTo(s"$currencyConverterEndpoint?cc=$currency"))
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
