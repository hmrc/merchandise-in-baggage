/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggage.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.calculation.CalculationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.currencyconversion.ConversionRatePeriod

import java.time.LocalDate.now
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CalculationServiceSpec extends BaseSpecWithApplication with ScalaFutures with MockFactory {

  val connector: CurrencyConversionConnector = mock[CurrencyConversionConnector]

  val service = new CalculationService(connector)

  "convert currency and calculate duty and vat for an item from outside the EU" in {
    (connector
      .getConversionRate(_: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returns(
        Future.successful(Seq(ConversionRatePeriod(now(), now(), "USD", BigDecimal(1.1))))
      )

    service
      .calculate(
        CalculationRequest(
          BigDecimal(100),
          Currency("USD", "USD", Some("USD"), List()),
          Country("US", "US", "US", isEu = false, List()),
          GoodsVatRates.Twenty
        )
      )
      .futureValue mustBe CalculationResult(AmountInPence(9091), AmountInPence(300), AmountInPence(1878))
  }

  "convert currency and calculate duty and vat for an item from inside the EU" in {
    (connector
      .getConversionRate(_: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returns(
        Future.successful(Seq(ConversionRatePeriod(now(), now(), "EUR", BigDecimal(1.1))))
      )

    service
      .calculate(
        CalculationRequest(
          BigDecimal(100),
          Currency("EUR", "EUR", Some("EUR"), List()),
          Country("FR", "FR", "FR", isEu = true, List()),
          GoodsVatRates.Twenty
        )
      )
      .futureValue mustBe CalculationResult(AmountInPence(9091), AmountInPence(0), AmountInPence(1818))
  }

  "calculate duty and vat for an item from a country that uses a GBP 1:1 currency" in {
    service
      .calculate(
        CalculationRequest(
          BigDecimal(100),
          Currency("GBP", "GBP", None, List()),
          Country("GB", "GB", "GB", isEu = true, List()),
          GoodsVatRates.Twenty
        )
      )
      .futureValue mustBe CalculationResult(AmountInPence(10000), AmountInPence(0), AmountInPence(2000))
  }

  "will return rate for a given currency code" in {
    val currency = Currency("EUR", "EUR", Some("EUR"), List())
    val conversionRatePeriods = Seq(
      ConversionRatePeriod(now(), now(), "EUR", BigDecimal(1.1)),
      ConversionRatePeriod(now(), now(), "ARS", BigDecimal(2.1)),
    )
    val findRate: String => Future[Seq[ConversionRatePeriod]] = _ => Future.successful(conversionRatePeriods)

    service.findRate(currency)(findRate).futureValue mustBe 1.1
    service.findRate(currency.copy(valueForConversion = Some("ARS")))(findRate).futureValue mustBe 2.1
  }

  "will return rate 1 for a given currency without valueForConversion or 0 if currency code is not found" in {
    val currency = Currency("EUR", "EUR", None, List())
    val conversionRatePeriods = Seq(ConversionRatePeriod(now(), now(), "EUR", BigDecimal(1.1)))
    val findRate: String => Future[Seq[ConversionRatePeriod]] = _ => Future.successful(conversionRatePeriods)

    service.findRate(currency)(findRate).futureValue mustBe 1
    service.findRate(currency.copy(valueForConversion = Some("X")))(findRate).futureValue mustBe 0
  }
}
