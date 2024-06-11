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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.{HttpClientSupport, WireMockSupport}
import uk.gov.hmrc.platforminitiatives.models.DependencyScope.Compile
import uk.gov.hmrc.platforminitiatives.models.Environment.Production
import uk.gov.hmrc.platforminitiatives.models.MetaArtefactDependency

class ServiceDependenciesConnectorSpec
  extends AnyWordSpec
     with Matchers
     with Results
     with MockitoSugar
     with GuiceOneAppPerSuite
     with HttpClientSupport
     with WireMockSupport
     with ScalaFutures
     with IntegrationPatience:

  given HeaderCarrier = HeaderCarrier()

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.service-dependencies.host" -> wireMockHost,
        "microservice.services.service-dependencies.port" -> wireMockPort
      ).build()

  private val connector = app.injector.instanceOf[ServiceDependenciesConnector]

  "ServiceDependenciesConnector.getMetaArtefactDependency" should {
    "return correct JSON for MetaArtefactDependency" in {
      stubFor(
        get(urlEqualTo(s"/api/repoDependencies?group=uk.gov.hmrc&artefact=test-dependency&versionRange=%5B0.0.0,)&flag=Production&scope=compile&repoType=Service"))
          .willReturn(aResponse().withBodyFile("service-dependencies/metaArtefactDependency.json"))
      )

      val dependencies = connector.getMetaArtefactDependency("uk.gov.hmrc", "test-dependency", Some(Production), List(Compile)).futureValue
      dependencies.head mustBe MetaArtefactDependency("hmrc-test", "uk.gov.hmrc", "test-dependency", "1.0.0", Seq("team1"))
    }
  }

  "ServiceDependenciesConnector.getSlugJdkVersions" should {
    "return correct JSON for Dependencies" in {
      stubFor(
        get(urlEqualTo(s"/api/jdkVersions"))
          .willReturn(aResponse().withBodyFile("service-dependencies/jdk-versions.json"))
      )

      val jdkVersions = connector.getSlugJdkVersions(team = None).futureValue
      jdkVersions.head.slugName mustBe "service-1"
    }
  }
