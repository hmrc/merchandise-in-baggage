package uk.gov.hmrc.merchandiseinbaggage.service

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.merchandiseinbaggage.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggage.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.{AmountInPence, CalculationResult, Country, Currency, GoodsVatRates}
import uk.gov.hmrc.merchandiseinbaggage.model.calculation.CalculationRequest
import uk.gov.hmrc.merchandiseinbaggage.model.currencyconversion.ConversionRatePeriod

import java.time.LocalDate.now
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CalculationServiceSpec extends BaseSpecWithApplication with ScalaFutures with MockFactory {

  val connector = mock[CurrencyConversionConnector]

  val service = new CalculationService(connector)

  "convert currency and calculate duty and vat for an item from outside the EU" in {
    (connector.getConversionRate(_: String))
      .expects(*)
      .returns(
        Future.successful(Seq(ConversionRatePeriod(now(), now(), "USD", BigDecimal(1.1))))
      )

    service.calculate(
      CalculationRequest(
        BigDecimal(100),
        Currency("USD", "USD", Some("USD"), List()),
        Country("US", "US", "US", false, List()),
        GoodsVatRates.Twenty
      )
    ).futureValue mustBe CalculationResult(AmountInPence(9091), AmountInPence(300), AmountInPence(1878))
  }

}
