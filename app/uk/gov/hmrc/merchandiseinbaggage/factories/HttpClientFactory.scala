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

package uk.gov.hmrc.merchandiseinbaggage.factories

import com.google.inject.{AbstractModule, Guice, Inject}
import play.api.inject.SimpleModule
import uk.gov.hmrc.http.HttpClient


trait HttpClientFactory {
  val httpClient: HttpClient
}

class HttpClientProvider @Inject()(client: HttpClient) extends javax.inject.Provider[HttpClientFactory] {
  override def get(): HttpClientFactory = new HttpClientFactory {
    override val httpClient: HttpClient = client
  }
}

object ServiceInjector extends SimpleModule {
  private val injector = Guice.createInjector(
    new AbstractModule {
      override def configure(): Unit = {
        play.api.inject.bind[HttpClientFactory].toProvider[HttpClientProvider]
      }
    }
  )
  def injectClient: HttpClient = injector.getProvider(classOf[HttpClient]).get()
}