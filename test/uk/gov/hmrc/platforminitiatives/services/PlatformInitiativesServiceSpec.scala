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

package uk.gov.hmrc.platforminitiatives.services

import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.platforminitiatives.connectors.ServiceDependenciesConnector
import uk.gov.hmrc.platforminitiatives.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PlatformInitiativesServiceSpec
  extends AnyWordSpec
     with Matchers
     with MockitoSugar
     with ScalaFutures
     with IntegrationPatience:

  "createUpgradeInitiative" should {
    "return an initiative for a Dependency Upgrade" in new Setup {
      when(mockServiceDependenciesConnector.getMetaArtefactDependency(
        group       = any[String],
        artefact    = any[String],
        environment = any[Option[Environment]],
        range       = any[String],
        scopes      = any[List[DependencyScope]])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(mockMetaArtefactDependencies))

      val result: PlatformInitiative =
        platformInitiativesService.createUpgradeInitiative(
          initiativeName        = "Test",
          initiativeDescription = "Test Description",
          group                 = "uk.gov.hmrc",
          artefact              = "Dep1",
          version               = Version(0, 2, 0),
          team                  = None,
          digitalService        = None
        ).futureValue
      result.initiativeName shouldBe "Test"
      result.progress       shouldBe Progress(2,3)
    }
  }

  "createJavaInitiative" should {
    "return an initiative for Java 11 upgrade" in new Setup {
      when(mockServiceDependenciesConnector.getSlugJdkVersions(team = any, digitalService = any)(using any[HeaderCarrier]))
        .thenReturn(Future.successful(mockSlugJdkVersions))
      val result: PlatformInitiative = platformInitiativesService.createJavaInitiative(
        initiativeName        = "Test",
        initiativeDescription = s"Test Description",
        version               = Version.apply("11.0.0"),
        team                  = None,
        digitalService        = None
      ).futureValue
      result.progress shouldBe Progress(1,2)
    }
  }

  "createMigrationInitiative" should {
    "return an initiative for a Migration and Dependency Upgrade when a targetVersion is supplied" in new Setup {
      when(mockServiceDependenciesConnector.getMetaArtefactDependency(
        group       = any[String],
        artefact    = eqTo("test-dependency"),
        environment = any[Option[Environment]],
        range       = any[String],
        scopes      = any[List[DependencyScope]])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(mockTestOldDependencies))
      when(mockServiceDependenciesConnector.getMetaArtefactDependency(
        group       = any[String],
        artefact    = eqTo("test-dependency-play-28"),
        environment = any[Option[Environment]],
        range       = any[String],
        scopes      = any[List[DependencyScope]])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(mockTestPlay28Dependencies))
      when(mockServiceDependenciesConnector.getMetaArtefactDependency(
        group       = any[String],
        artefact    = eqTo("test-dependency-play-29"),
        environment = any[Option[Environment]],
        range       = any[String],
        scopes      = any[List[DependencyScope]])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(mockTestPlay29Dependencies))
      when(mockServiceDependenciesConnector.getMetaArtefactDependency(
        group       = any[String],
        artefact    = eqTo("test-dependency-play-30"),
        environment = any[Option[Environment]],
        range       = any[String],
        scopes      = any[List[DependencyScope]])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(mockTestPlay30Dependencies))

      val result: PlatformInitiative =
        platformInitiativesService.createMigrationInitiative(
          initiativeName        = "Test",
          initiativeDescription = "Test Description",
          fromArtefacts         = Seq(Artefact("uk.gov.hmrc", "test-dependency")),
          toArtefacts           = Seq(Artefact("uk.gov.hmrc", "test-dependency-play-28"), Artefact("uk.gov.hmrc", "test-dependency-play-29"), Artefact("uk.gov.hmrc", "test-dependency-play-30")),
          targetVersion         = Some(Version(0, 2, 0))
        ).futureValue
      result.initiativeName shouldBe "Test"
      result.progress       shouldBe Progress(3,5)
    }

    "return an initiative for a Migration when no targetVersion is supplied" in new Setup {
      when(mockServiceDependenciesConnector.getMetaArtefactDependency(
        group       = any[String],
        artefact    = eqTo("test-dependency"),
        environment = any[Option[Environment]],
        range       = any[String],
        scopes      = any[List[DependencyScope]])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(mockTestOldDependencies))
      when(mockServiceDependenciesConnector.getMetaArtefactDependency(
        group       = any[String],
        artefact    = eqTo("test-dependency-play-28"),
        environment = any[Option[Environment]],
        range       = any[String],
        scopes      = any[List[DependencyScope]])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(mockTestPlay28Dependencies))
      when(mockServiceDependenciesConnector.getMetaArtefactDependency(
        group       = any[String],
        artefact    = eqTo("test-dependency-play-29"),
        environment = any[Option[Environment]],
        range       = any[String],
        scopes      = any[List[DependencyScope]])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(mockTestPlay29Dependencies))
      when(mockServiceDependenciesConnector.getMetaArtefactDependency(
        group       = any[String],
        artefact    = eqTo("test-dependency-play-30"),
        environment = any[Option[Environment]],
        range       = any[String],
        scopes      = any[List[DependencyScope]])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(mockTestPlay30Dependencies))

      val result: PlatformInitiative =
        platformInitiativesService.createMigrationInitiative(
          initiativeName        = "Test",
          initiativeDescription = "Test Description",
          fromArtefacts         = Seq(Artefact("uk.gov.hmrc", "test-dependency")),
          toArtefacts           = Seq(Artefact("uk.gov.hmrc", "test-dependency-play-28"), Artefact("uk.gov.hmrc", "test-dependency-play-29"), Artefact("uk.gov.hmrc", "test-dependency-play-30")),
          targetVersion         = None
        ).futureValue
      result.initiativeName shouldBe "Test"
      result.progress       shouldBe Progress(4,5)
    }
  }

  "platformInitiatives" should {
    "return all initiatives" in new Setup {
      when(mockConfiguration.get[Boolean]("initiatives.service.includeExperimental"))
        .thenReturn(true)
      when(mockServiceDependenciesConnector.getMetaArtefactDependency(
        group       = any[String],
        artefact    = any[String],
        environment = any[Option[Environment]],
        range       = any[String],
        scopes      = any[List[DependencyScope]])(using any[HeaderCarrier])
      ).thenReturn(Future.successful(mockMetaArtefactDependencies))
      when(mockServiceDependenciesConnector.getSlugJdkVersions(team = any, digitalService = any)(using any[HeaderCarrier]))
        .thenReturn(Future.successful(mockSlugJdkVersions))
      val result: Seq[PlatformInitiative] = platformInitiativesService.platformInitiatives(teamName = None, digitalService = None).futureValue
      result.length shouldBe 8
    }
  }

  private[this] trait Setup {
    given HeaderCarrier = HeaderCarrier()
    val includeExperimental = false
    val mockConfiguration                : Configuration                 = mock[Configuration]
    val mockServiceDependenciesConnector : ServiceDependenciesConnector  = mock[ServiceDependenciesConnector]
    val mockControllerComponents         : ControllerComponents          = mock[ControllerComponents]
    lazy val platformInitiativesService  : PlatformInitiativesService    = new PlatformInitiativesService(mockConfiguration, mockServiceDependenciesConnector, mockControllerComponents)

    val mockMetaArtefactDependencies = Seq(
      MetaArtefactDependency(
        repoName       = "hmrc-test",
        depGroup       = "uk.gov.hmrc",
        depArtefact    = "test-dependency",
        depVersion     = "1.0.0",
        teams          = Seq("team-2"),
        digitalService = None
      ),
      MetaArtefactDependency(
        repoName       = "hmrc-test",
        depGroup       = "uk.gov.hmrc",
        depArtefact    = "test-dependency",
        depVersion     = "1.5.0",
        teams          = Seq("team-1"),
        digitalService = None
      ),
      MetaArtefactDependency(
        repoName       = "hmrc-test",
        depGroup       = "uk.gov.hmrc",
        depArtefact    = "test-dependency",
        depVersion     = "0.1.0",
        teams          = Seq("team-3"),
        digitalService = None
      )
    )

    val mockTestOldDependencies = Seq(
      MetaArtefactDependency(
        repoName       = "hmrc-test",
        depGroup       = "uk.gov.hmrc",
        depArtefact    = "test-dependency",
        depVersion     = "1.0.0",
        teams          = Seq("team-2"),
        digitalService = None
      )
    )

    val mockTestPlay28Dependencies = Seq(
      MetaArtefactDependency(
        repoName       = "hmrc-test",
        depGroup       = "uk.gov.hmrc",
        depArtefact    = "test-dependency-play-28",
        depVersion     = "0.1.0",
        teams          = Seq("team-1"),
        digitalService = None
      ),
      MetaArtefactDependency(
        repoName       = "hmrc-test",
        depGroup       = "uk.gov.hmrc",
        depArtefact    = "test-dependency-play-28",
        depVersion     = "1.8.0",
        teams          = Seq("team-3"),
        digitalService = None
      )
    )

    val mockTestPlay29Dependencies = Seq(
      MetaArtefactDependency(
        repoName       = "hmrc-test",
        depGroup       = "uk.gov.hmrc",
        depArtefact    = "test-dependency-play-29",
        depVersion     = "1.9.0",
        teams          = Seq("team-4"),
        digitalService = None
      )
    )

    val mockTestPlay30Dependencies = Seq(
      MetaArtefactDependency(
        repoName       = "hmrc-test",
        depGroup       = "uk.gov.hmrc",
        depArtefact    = "test-dependency-play-30",
        depVersion     = "1.2.0",
        teams          = Seq("team-5"),
        digitalService = None
      )
    )

    val mockSlugJdkVersions = Seq(
      SlugJdkVersion(
        slugName    = "service-1",
        version     = Version.apply("1.8.0_345"),
        vendor      = "OPENJDK",
        kind        = "JRE",
      ),
      SlugJdkVersion(
        slugName    = "service-2",
        version     = Version.apply("11.0.15"),
        vendor      = "OPENJDK",
        kind        = "JRE",
      )
    )
  }
