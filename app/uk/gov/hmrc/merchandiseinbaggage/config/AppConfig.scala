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

package uk.gov.hmrc.merchandiseinbaggage.config

import javax.inject.Singleton
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfigSource._
import pureconfig.generic.auto._ // Do not remove this

@Singleton
class AppConfig() extends AuthConfiguration with MongoConfiguration

trait AuthConfiguration {
  lazy val authConfig: AuthConfig = configSource("microservice.services.auth").loadOrThrow[AuthConfig]
  import authConfig._
  lazy val authBaseUrl = s"$protocol://$host:$port"
}

trait MongoConfiguration {
  lazy val mongoConf: MongoConf = configSource("mongodb").loadOrThrow[MongoConf]
}

final case class MongoConf(uri: String, host: String = "localhost", port: Int = 27017, collectionName: String = "declaration")
final case class AuthConfig(protocol: String, host: String, port: Int)