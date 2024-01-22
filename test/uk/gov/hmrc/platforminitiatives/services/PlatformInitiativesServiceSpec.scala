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

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.platforminitiatives.connectors.{RepositoryDisplayDetails, ServiceDependenciesConnector, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.platforminitiatives.models.{PlatformInitiative, Progress, SlugDependencies, SlugJdkVersion, Version}

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PlatformInitiativesServiceSpec
  extends AnyWordSpec
     with Matchers
     with MockitoSugar
     with ArgumentMatchersSugar
     with ScalaFutures
     with IntegrationPatience {

  "createDefaultBranchInitiative" should {
    "return an initiative for DefaultBranches where branch name is not updated" in new Setup {
      when(mockTeamsAndRepositoriesConnector.allDefaultBranches()(any[HeaderCarrier]))
        .thenReturn(Future.successful(mockRepositories))
      val result: PlatformInitiative =
        platformInitiativesService.createDefaultBranchInitiative(
          initiativeName        = "Test",
          initiativeDescription = "Test Description",
          completedLegend       = "Updated",
          inProgressLegend      = "Master"
        ).futureValue
      result.progress shouldBe Progress(2,3)
    }
  }

  "createUpgradeInitiative" should {
    "return an initiative for a Dependency Upgrade" in new Setup {
      when(mockServiceDependenciesConnector.getServiceDependency(
        group       = any[String],
        artefact    = any[String],
        environment = any,
        range       = any[String])(any[HeaderCarrier])
      ).thenReturn(Future.successful(mockSlugDependencies))

      val result: PlatformInitiative =
        platformInitiativesService.createUpgradeInitiative(
          initiativeName        = "Test",
          initiativeDescription = "Test Description",
          group                 = "uk.gov.hmrc",
          artefact              = "Dep1",
          version               = Version(0, 2, 0)
        ).futureValue
      result.initiativeName shouldBe "Test"
      result.progress       shouldBe Progress(2,2)
    }
  }

  "createJavaInitiative" should {
    "return an initiative for Java 11 upgrade" in new Setup {
      when(mockServiceDependenciesConnector.getSlugJdkVersions(team = any)(any[HeaderCarrier]))
        .thenReturn(Future.successful(mockSlugJdkVersions))
      val result: PlatformInitiative = platformInitiativesService.createJavaInitiative(
        initiativeName        = "Test",
        initiativeDescription = s"Test Description",
        version               = Version.apply("11.0.0"),
      ).futureValue
      result.progress shouldBe Progress(1,2)
    }
  }

  "allPlatformInitiatives" should {
    "return all initiatives" in new Setup {
      when(mockConfiguration.get[Boolean]("initiatives.service.includeExperimental"))
        .thenReturn(true)
      when(mockTeamsAndRepositoriesConnector.allDefaultBranches()(any[HeaderCarrier]))
        .thenReturn(Future.successful(mockRepositories))
      when(mockServiceDependenciesConnector.getServiceDependency(
          group       = any[String],
          artefact    = any[String],
          environment = any,
          range       = any[String])(any[HeaderCarrier])
        )
        .thenReturn(Future.successful(mockSlugDependencies))
      when(mockServiceDependenciesConnector.getSlugJdkVersions(team = any)(any[HeaderCarrier]))
        .thenReturn(Future.successful(mockSlugJdkVersions))
      val result: Seq[PlatformInitiative] = platformInitiativesService.allPlatformInitiatives().futureValue
      result.length shouldBe 8
    }
  }

  private[this] trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockConfiguration                : Configuration                 = mock[Configuration]
    val mockTeamsAndRepositoriesConnector: TeamsAndRepositoriesConnector = mock[TeamsAndRepositoriesConnector]
    val mockServiceDependenciesConnector : ServiceDependenciesConnector  = mock[ServiceDependenciesConnector]
    val mockControllerComponents         : ControllerComponents          = mock[ControllerComponents]
    val platformInitiativesService       : PlatformInitiativesService    = new PlatformInitiativesService(mockConfiguration, mockTeamsAndRepositoriesConnector, mockServiceDependenciesConnector, mockControllerComponents)

    val mockRepositories: Seq[RepositoryDisplayDetails] = Seq(
      RepositoryDisplayDetails(
        name          = "test",
        createdAt     = Instant.now(),
        lastUpdatedAt = Instant.now(),
        isArchived    = false,
        teamNames     = Seq("Team-1", "Team-2"),
        defaultBranch = "main"
      ),
      RepositoryDisplayDetails(
        name          = "test-2",
        createdAt     = Instant.now(),
        lastUpdatedAt = Instant.now(),
        isArchived    = false,
        teamNames     = Seq("Team-1"),
        defaultBranch = "master"
      ),
      RepositoryDisplayDetails(
        name          = "test-3",
        createdAt     = Instant.now(),
        lastUpdatedAt = Instant.now(),
        isArchived    = false,
        teamNames     = Seq("Team-1"),
        defaultBranch = "main"
      ),
      RepositoryDisplayDetails(
        name          = "test-4",
        createdAt     = Instant.now(),
        lastUpdatedAt = Instant.now(),
        isArchived    = true,
        teamNames     = Seq("Team-2"),
        defaultBranch = "master"
      )
    )

    val mockSlugDependencies = Seq(
      SlugDependencies(
        slugName    = "hmrc-test",
        depGroup    = "uk.gov.hmrc",
        depArtefact = "test-dependency",
        depVersion  = "1.0.0",
        teams       = Seq("team-2")
      ),
      SlugDependencies(
        slugName    = "hmrc-test",
        depGroup    = "uk.gov.hmrc",
        depArtefact = "test-dependency",
        depVersion  = "1.5.0",
        teams       = Seq("team-1")
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
}
