/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.service

import java.time.LocalDate

import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggage.model.api.{CalculationRequest, CurrencyConversionResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{AmountInPence, CurrencyNotFound}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CustomsDutyCalculatorSpec extends BaseSpecWithApplication with ScalaFutures {

  implicit val hc = HeaderCarrier()

  "will convert currency in GBP and calculate a customs duty in pounds and pence" in new CustomsDutyCalculator {
    val client = injector.instanceOf[HttpClient]
    override def findCurrencyRate(httpClient: HttpClient, currencyCode: String, date: LocalDate)
                                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[CurrencyConversionResponse]] =
      Future.successful(List(CurrencyConversionResponse("USD", Some("1.3064"))))

    val eventualAmountInPence = customDuty(client, CalculationRequest("USD", AmountInPence(100.0))).value

    eventualAmountInPence.futureValue mustBe Right(AmountInPence(7.65))
  }

  "will return a failure if currency is not found" in new CustomsDutyCalculator {
    val client = injector.instanceOf[HttpClient]
    override def findCurrencyRate(httpClient: HttpClient, currencyCode: String, date: LocalDate)
                                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[List[CurrencyConversionResponse]] =
      Future.successful(List(CurrencyConversionResponse("USD", None)))

    val eventualAmountInPence = customDuty(client, CalculationRequest("USD", AmountInPence(100.0))).value

    eventualAmountInPence.futureValue mustBe Left(CurrencyNotFound)
  }
}
