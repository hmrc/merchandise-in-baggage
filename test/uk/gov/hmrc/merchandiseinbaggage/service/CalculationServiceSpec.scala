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

import java.time.LocalDate.now

import com.softwaremill.quicklens._
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.{ConversionRatePeriod, YesNoDontKnow}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CalculationServiceSpec extends BaseSpecWithApplication with ScalaFutures with MockFactory with CoreTestData {

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
      .calculate(aCalculationRequest(100, "USD", YesNoDontKnow.No))
      .futureValue mustBe aCalculationResult(9091, 300, 1878, Some("USD"), Some(1.1))
  }

  "convert currency and calculate duty and vat for an item from inside the EU" in {
    (connector
      .getConversionRate(_: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *)
      .returns(
        Future.successful(Seq(ConversionRatePeriod(now(), now(), "EUR", BigDecimal(1.1))))
      )

    service
      .calculate(aCalculationRequest(100, "EUR"))
      .futureValue mustBe aCalculationResult(9091, 0, 1818, Some("EUR"), Some(1.1))
  }

  "calculate duty and vat for an item from a country that uses a GBP 1:1 currency" in {
    service
      .calculate(
        aCalculationRequest(100, "GBP")
          .modify(_.currency.valueForConversion)
          .setTo(Option.empty))
      .futureValue mustBe aCalculationResult(10000, 0, 2000)
  }
}
