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
import uk.gov.hmrc.platforminitiatives.model.{DependencyScope, Environment, MetaArtefactDependency}

import scala.concurrent.ExecutionContext

class ServiceDependenciesConnectorSpec
  extends AnyWordSpec
     with Matchers
     with MockitoSugar
     with HttpClientV2Support
     with WireMockSupport
     with ScalaFutures
     with IntegrationPatience:

  import ExecutionContext.Implicits.global

  given HeaderCarrier = HeaderCarrier()

  val config = Configuration(
    "microservice.services.service-dependencies.host" -> wireMockHost,
    "microservice.services.service-dependencies.port" -> wireMockPort
  )

  private val connector = ServiceDependenciesConnector(
    httpClientV2   = httpClientV2,
    servicesConfig = ServicesConfig(config)
  )

  "ServiceDependenciesConnector.getMetaArtefactDependency" should:
    "return correct JSON for MetaArtefactDependency" in:
      stubFor:
        get(urlEqualTo(s"/api/repoDependencies?group=uk.gov.hmrc&artefact=test-dependency&versionRange=%5B0.0.0,)&flag=Production&scope=compile&repoType=Service"))
          .willReturn(aResponse().withBodyFile("service-dependencies/metaArtefactDependency.json"))

      val dependencies = connector.getMetaArtefactDependency("uk.gov.hmrc", "test-dependency", Some(Environment.Production), Seq(DependencyScope.Compile)).futureValue
      dependencies.head shouldBe MetaArtefactDependency("hmrc-test", "uk.gov.hmrc", "test-dependency", "1.0.0", Seq("team1"), digitalService = None)

  "ServiceDependenciesConnector.getSlugJdkVersions" should:
    "return correct JSON for Dependencies" in:
      stubFor:
        get(urlEqualTo(s"/api/jdkVersions"))
          .willReturn(aResponse().withBodyFile("service-dependencies/jdk-versions.json"))

      val jdkVersions = connector.getSlugJdkVersions(team = None, digitalService = None).futureValue
      jdkVersions.head.slugName shouldBe "service-1"
