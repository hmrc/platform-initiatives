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

package uk.gov.hmrc.platforminitiatives.connectors

import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.mvc.Results
import uk.gov.hmrc.http.HeaderCarrier
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.test.{HttpClientSupport, WireMockSupport}

import scala.io.Source.fromInputStream

class TeamsAndRepositoriesConnectorSpec
  extends AnyWordSpec
    with Matchers
    with Results
    with MockitoSugar
    with GuiceOneAppPerSuite
    with HttpClientSupport
    with WireMockSupport {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  override lazy val resetWireMockMappings = false

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.teams-and-repositories.host" -> wireMockHost,
        "microservice.services.teams-and-repositories.port" -> wireMockPort,
        "play.http.requestHandler" -> "play.api.http.DefaultHttpRequestHandler",
        "metrics.jvm" -> false
      ).build()

  private val connector = app.injector.instanceOf[TeamsAndRepositoriesConnector]

  "TeamsAndRepositoriesConnector.allDefaultBranches" should {
    "return correct JSON for Repository Display Details" in {
      stubFor(
        get(urlEqualTo(s"/api/repositories"))
          .willReturn(aResponse().withBody(getFile("/teams-and-repositories/repositories.json")))
      )
      val dependencies = connector.allDefaultBranches.futureValue
      dependencies.head.name mustBe "test"
    }
  }

  private def getFile(path: String) = fromInputStream(getClass.getResourceAsStream(path)).mkString
}