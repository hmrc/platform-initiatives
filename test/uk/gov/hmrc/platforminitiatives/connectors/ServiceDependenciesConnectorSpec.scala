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

package uk.gov.hmrc.platforminitiatives.connectors

import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.mvc.Results
import uk.gov.hmrc.http.HeaderCarrier
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.test.{HttpClientSupport, WireMockSupport}

import scala.language.postfixOps

class ServiceDependenciesConnectorSpec
  extends AnyWordSpec
    with Matchers
    with Results
    with MockitoSugar
    with GuiceOneAppPerSuite
    with HttpClientSupport
    with WireMockSupport
    with ScalaFutures
    with IntegrationPatience {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  override lazy val resetWireMockMappings = false
  override lazy val wireMockRootDirectory = "test/resources"

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.service-dependencies.host" -> wireMockHost,
        "microservice.services.service-dependencies.port" -> wireMockPort,
        "play.http.requestHandler" -> "play.api.http.DefaultHttpRequestHandler",
        "metrics.jvm" -> false
      ).build()

  private val connector = app.injector.instanceOf[ServiceDependenciesConnector]

  "ServiceDependenciesConnector.getAllDependencies" should {
    "return correct JSON for Dependencies" in {

      stubFor(
        get(urlEqualTo(s"/api/dependencies"))
          .willReturn(aResponse().withBodyFile("/service-dependencies/dependencies.json"))
      )
      val dependencies = connector.getAllDependencies().futureValue
      dependencies.head.repositoryName mustBe "hmrc-test"
    }
  }
}
