package uk.gov.hmrc.merchandiseinbaggage.factories

import com.google.inject.{AbstractModule, Guice}
import play.api.inject.SimpleModule
import uk.gov.hmrc.http.HttpClient

object ServiceFactory extends SimpleModule {
  private val injector = Guice.createInjector(
    new AbstractModule {
      override def configure(): Unit = {
        play.api.inject.bind[HttpClientFactory].toProvider[HttpClientProvider]
      }
    }
  )
  def httpClient: HttpClient = injector.getProvider(classOf[HttpClient]).get()
}

trait ServiceFactory {
  def httpClient: HttpClient = ServiceFactory.httpClient
}