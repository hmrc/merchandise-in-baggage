/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDate

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggage.model.api.{CalculationRequest, CurrencyConversionResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Amount, CurrencyNotFound, ForeignAmount}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CustomsDutyCalculatorSpec extends BaseSpecWithApplication with ScalaFutures {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "will convert currency in GBP and calculate a customs duty in pounds and pence" in new CustomsDutyCalculator {
    override val httpClient = injector.instanceOf[HttpClient]
    override def findCurrencyRate(currencyCode: String, date: LocalDate)
                                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[CurrencyConversionResponse]] =
      Future.successful(List(CurrencyConversionResponse("USD", Some("1.3064"))))

    private val eventualAmountInPence = customDuty(CalculationRequest("USD", ForeignAmount(100))).value

    eventualAmountInPence.futureValue mustBe Right(Amount(7.654623392529088))
  }

  "will convert currency in GBP and calculate a customs duty in pounds and pence for a foreign amount with decimals" in new CustomsDutyCalculator {
    override def findCurrencyRate(currencyCode: String, date: LocalDate)
                                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[CurrencyConversionResponse]] =
      Future.successful(List(CurrencyConversionResponse("USD", Some("1.2763"))))

    private val eventualAmountInPence = customDuty(CalculationRequest("USD", ForeignAmount(122222.56))).value

    eventualAmountInPence.futureValue mustBe Right(Amount(9576.319047245945))
  }

  "will return a failure if currency is not found" in new CustomsDutyCalculator {
    override val httpClient = injector.instanceOf[HttpClient]
    override def findCurrencyRate(currencyCode: String, date: LocalDate)
                                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[CurrencyConversionResponse]] =
      Future.successful(List(CurrencyConversionResponse("USD", None)))

    private val eventualAmountInPence = customDuty(CalculationRequest("USD", ForeignAmount(100))).value

    eventualAmountInPence.futureValue mustBe Left(CurrencyNotFound)
  }
}
