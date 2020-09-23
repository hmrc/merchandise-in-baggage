/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.model.core

import play.api.libs.json.{JsError, JsResult, JsString, JsSuccess, JsValue, Json, Reads, Writes}

sealed trait Currency
case object GBP extends Currency


object Currency {
  implicit val write = new Writes[Currency] {
    override def writes(status: Currency): JsValue = status match {
      case GBP => JsString("GBP")
      case _   => JsString("currency not found") //TODO add currencies
    }
  }

  implicit val read = new Reads[Currency] {
    override def reads(value: JsValue): JsResult[Currency] = value match {
      case JsString("GBP") => JsSuccess(GBP)
      case _               => JsError("invalid value")
    }
  }
}


