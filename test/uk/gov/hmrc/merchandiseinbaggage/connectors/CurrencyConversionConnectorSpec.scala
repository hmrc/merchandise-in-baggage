/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.connectors

import java.time.LocalDate

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.merchandiseinbaggage.model.api.CurrencyConversionResponse
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CurrencyConversionStub}

import scala.concurrent.ExecutionContext.Implicits.global

class CurrencyConversionConnectorSpec extends BaseSpecWithApplication with CurrencyConversionStub with ScalaFutures {

  "retrieve currency conversion" in new CurrencyConversionConnector {
    val client = injector.instanceOf[HttpClient]
    val currencyCode = "USD"
    val conversionResponse: CurrencyConversionResponse = CurrencyConversionResponse(currencyCode, "1.3064")

    getCurrencyConversionStub(currencyCode)
    findCurrencyConversion(client, currencyCode, LocalDate.now).futureValue mustBe List(conversionResponse)
  }
}
