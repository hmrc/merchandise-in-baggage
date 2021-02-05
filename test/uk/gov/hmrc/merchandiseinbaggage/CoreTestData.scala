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

package uk.gov.hmrc.merchandiseinbaggage

import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.{Address, AddressLookupCountry}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationRequest, CalculationResult}
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.{CheckEoriAddress, CheckResponse, CompanyDetails}
import java.time.{LocalDate, LocalDateTime}
import java.util.UUID.randomUUID

trait CoreTestData {

  def aDeclarationId: DeclarationId = DeclarationId(randomUUID().toString)

  private val today = LocalDate.now
  private val aSessionId = SessionId("123456789")
  private val aGoodDestination = GreatBritain
  val aImportGoods: ImportGoods = ImportGoods(
    CategoryQuantityOfGoods("test", "1"),
    GoodsVatRates.Five,
    YesNoDontKnow.Yes,
    PurchaseDetails("100", Currency("GBP", "title.euro_eur", Some("GBP"), List("Europe", "European")))
  )
  private val aDeclarationGoods = DeclarationGoods(Seq(aImportGoods))
  private val aName = Name("Terry", "Crews")
  private val anEori = Eori("eori-test")
  private val anEmail = Email("someone@")
  private val aJourneyDetails = JourneyOnFoot(Port("DVR", "title.dover", isGB = true, List("Port of Dover")), LocalDate.now())
  private val aMibReference = MibReference("mib-ref-1234")
  private val paymentCalculations = PaymentCalculations(
    aDeclarationGoods.goods
      .asInstanceOf[Seq[ImportGoods]]
      .map(good => PaymentCalculation(good, CalculationResult(good, AmountInPence(100), AmountInPence(100), AmountInPence(100), None))))
  private val totalCalculationResult =
    TotalCalculationResult(paymentCalculations, AmountInPence(100), AmountInPence(100), AmountInPence(100), AmountInPence(100))

  def aDeclaration: Declaration =
    Declaration(
      aDeclarationId,
      aSessionId,
      Import,
      aGoodDestination,
      aDeclarationGoods,
      aName,
      Some(anEmail),
      None,
      anEori,
      aJourneyDetails,
      LocalDateTime.now,
      aMibReference,
      Some(totalCalculationResult)
    )

  val aCustomsAgent: CustomsAgent =
    CustomsAgent("Andy Agent", Address(Seq("1 Agent Drive", "Agent Town"), Some("AG1 5NT"), AddressLookupCountry("GB", Some("UK"))))

  val aJourneyInASmallVehicle: JourneyInSmallVehicle =
    JourneyInSmallVehicle(Port("DVR", "title.dover", isGB = true, List("Port of Dover")), LocalDate.now(), "licence")

  val aCheckEoriAddress = CheckEoriAddress("999 High Street", "CityName", "SS99 1AA")
  val aCompanyDetails = CompanyDetails("Firstname LastName", aCheckEoriAddress)

  val aCheckResponse = CheckResponse("GB025115110987654", true, Some(aCompanyDetails))

  val aSuccessCheckResponse =
    """[
      |  {
      |    "eori": "GB025115110987654",
      |    "valid": true,
      |    "companyDetails": {
      |      "traderName": "Firstname LastName",
      |      "address": {
      |        "streetAndNumber": "999 High Street",
      |        "cityName": "CityName",
      |        "postcode": "SS99 1AA"
      |      }
      |    },
      |    "processingDate": "2021-01-27T11:00:22.522Z[Europe/London]"
      |  }
      |]""".stripMargin

  def aCalculationRequest(amount: Int, currency: String, producedInEU: YesNoDontKnow = YesNoDontKnow.Yes) = CalculationRequest(
    aImportGoods,
    BigDecimal(amount),
    Currency(currency, currency, Some(currency), Nil),
    producedInEU,
    GoodsVatRates.Twenty
  )

  def aCalculationResult(gbp: Int, duty: Int, vat: Int, currency: Option[String] = None, conversion: Option[Double] = None) =
    conversion.fold(CalculationResult(aImportGoods, AmountInPence(gbp), AmountInPence(duty), AmountInPence(vat), None))(
      conv =>
        CalculationResult(
          aImportGoods,
          AmountInPence(gbp),
          AmountInPence(duty),
          AmountInPence(vat),
          Some(ConversionRatePeriod(today, today, currency.getOrElse(""), conv))))
}
