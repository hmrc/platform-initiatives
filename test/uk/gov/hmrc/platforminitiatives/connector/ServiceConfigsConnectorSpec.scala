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

package uk.gov.hmrc.platforminitiatives.connector

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.platforminitiatives.model.Environment

import scala.concurrent.ExecutionContext

class ServiceConfigsConnectorSpec
  extends AnyWordSpec
     with Matchers
     with MockitoSugar
     with HttpClientV2Support
     with WireMockSupport
     with ScalaFutures
     with IntegrationPatience:

  import ExecutionContext.Implicits.global
  import ServiceConfigsConnector.*

  given HeaderCarrier = HeaderCarrier()

  val config = Configuration(
    "microservice.services.service-configs.host" -> wireMockHost,
    "microservice.services.service-configs.port" -> wireMockPort
  )

  private val connector = ServiceConfigsConnector(
    httpClientV2   = httpClientV2,
    servicesConfig = ServicesConfig(config)
  )

  "ServiceDependenciesConnector.getMetaArtefactDependency" should:
    "return correct JSON for MetaArtefactDependency" in:
      stubFor:
        get(urlEqualTo(s"/service-configs/search?teamName=t&valueFilterType=equalTo&digitalService=ds&key=k&value=v&keyFilterType=equalTo&environment=production"))
          .willReturn:
            aResponse().withBody(
              """[
                {"serviceName":"service1"},
                {"serviceName":"service2"}
              ]"""
            )

      val configs =
        connector.searchConfig(
          key            = "k",
          value          = "v",
          environment    = Seq(Environment.Production),
          team           = Some("t"),
          digitalService = Some("ds")
        ).futureValue

      configs shouldBe Seq(Config("service1"), Config("service2"))
