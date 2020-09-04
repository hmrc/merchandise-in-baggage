/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.util

import play.api.libs.json._

object ValueClassFormat {
  def format[A: Format](fromStringToA: String => A)(fromAToString: A => String) =
    Format[A](
      Reads[A] {
        case JsString(str) => JsSuccess(fromStringToA(str))
        case unknown       => JsError(s"JsString value expected, got: $unknown")
      },
      Writes[A](a => JsString(fromAToString(a)))
    )

  def formatDouble[A: Format](fromNumberToA: Double => A)(fromAToDouble: A => Double) =
    Format[A](
      Reads[A] {
        case JsNumber(n) => JsSuccess(fromNumberToA(n.toDouble))
        case unknown     => JsError(s"JsString value expected, got: $unknown")
      },
      Writes[A](a => JsNumber(fromAToDouble(a)))
    )
}
