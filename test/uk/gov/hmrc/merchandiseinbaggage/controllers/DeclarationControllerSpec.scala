/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggage.controllers

import akka.stream.Materializer
import org.scalatestplus.play.WsScalaTestClient
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import play.api.libs.ws.ahc.AhcWSClient
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationIdResponse
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Declaration, DeclarationId}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

import scala.concurrent.ExecutionContext.Implicits.global

class DeclarationControllerSpec extends BaseSpecWithApplication with CoreTestData with Status with WsScalaTestClient {
  private implicit val mat: Materializer = injector.instanceOf[Materializer]
  private implicit val wsClient: AhcWSClient = AhcWSClient()

  override def beforeEach(): Unit = repository.deleteAll().futureValue

  "post to /merchandise-in-baggage/declarations will save a declaration" in {
    val response = wsUrl("/merchandise-in-baggage/declarations").post(toJson(aDeclarationRequest)).futureValue
    val id = Json.parse(response.body).as[DeclarationIdResponse].id
    val persisted: Option[Declaration] = repository.findByDeclarationId(id).futureValue

    response.status mustBe CREATED
    persisted.isDefined mustBe true
  }

  "get of /merchandise-in-baggage/declarations/id" should {
    "return a declaration" when {
      "the declaration id is found" in {
        val persisted =
          (for {
            persisted <- repository.insert(aDeclaration)
            _ <- repository.insert(aDeclarationRequest.toDeclaration)
          } yield persisted).futureValue

        repository.findAll.futureValue.size mustBe 2

        val id = persisted.declarationId
        val response = wsUrl(s"/merchandise-in-baggage/declarations/${id.value}").get().futureValue
        val found = Json.parse(response.body).as[Declaration]

        response.status mustBe OK
        found mustBe persisted
      }
    }

    "return 404" when {
      "the declaration id is not found" in {
        wsUrl(s"/merchandise-in-baggage/declarations/${DeclarationId().value}").get().futureValue.status mustBe NOT_FOUND
      }
    }
  }
}
