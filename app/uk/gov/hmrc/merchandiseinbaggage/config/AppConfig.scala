/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.config

import javax.inject.Singleton
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfigSource._
import pureconfig.generic.auto._ // Do not remove this

@Singleton
class AppConfig() extends MongoConfiguration with EmailConfiguration with EoriCheckConfiguration with CurrencyConversionConfiguration {
  lazy val bfEmail: String = configSource("BF.email").loadOrThrow[String]
}

trait MongoConfiguration {
  lazy val mongoConf: MongoConf = configSource("mongodb").loadOrThrow[MongoConf]
}

final case class MongoConf(uri: String, host: String = "localhost", port: Int = 27017, collectionName: String = "declaration")

trait EmailConfiguration {
  lazy val emailConf: EmailConf = configSource("microservice.services.email").loadOrThrow[EmailConf]
}

final case class EmailConf(host: String = "localhost", port: Int = 8300, protocol: String) {
  val url = "/transactionengine/email"
}

trait EoriCheckConfiguration {
  lazy val eoriCheckConf: EoriCheckConf = configSource("microservice.services.eori-check").loadOrThrow[EoriCheckConf]
}

final case class EoriCheckConf(protocol: String, host: String = "localhost", port: Int) {
  val eoriCheckUrl = s"/check-eori-number/check-eori/"
}

trait CurrencyConversionConfiguration {
  lazy val currencyConversionConf: CurrencyConversionConf = configSource("microservice.services.currency-conversion")
    .loadOrThrow[CurrencyConversionConf]
}

final case class CurrencyConversionConf(protocol: String, host: String = "localhost", port: Int) {
  val currencyConversionUrl = s"/currency-conversion/rates/"
}
