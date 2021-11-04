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

package uk.gov.hmrc.platforminitiatives.services

import org.mockito.MockitoSugar
import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.platforminitiatives.connectors.{RepositoryDisplayDetails, ServiceDependenciesConnector, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.platforminitiatives.models.{Dependencies, Dependency, PlatformInitiative, Version}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class PlatformInitiativesServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  "createDefaultBranchInitiative" should {
    "return an initiative for DefaultBranches where branch name is not updated" in new Setup {
      when(mockTeamsAndRepositoriesConnector.allDefaultBranches(any[HeaderCarrier])) thenReturn
        Future.successful(mockRepositories)
      val result: Future[PlatformInitiative] = platformInitiativesService.createDefaultBranchInitiative(
        initiativeName        = "Test",
        initiativeDescription = "Test Description",
        completedLegend       = "Updated",
        inProgressLegend      = "Master"
      )
      val finalResult: PlatformInitiative = result.futureValue

      finalResult.currentProgress shouldBe 2
    }
  }

  "createUpgradeInitiative" should {
    "return an initiative for a Dependency Upgrade" in new Setup {
      when(mockServiceDependenciesConnector.getAllDependencies()(any[HeaderCarrier])) thenReturn
        Future.successful(mockDependencies)
      val result: Future[PlatformInitiative] = platformInitiativesService.createUpgradeInitiative(
        initiativeName        = "Test",
        initiativeDescription = "Test Description",
        dependencyName        = "Dep1",
        version               = Option(Version(0, 2, 0))
      )
      val finalResult: PlatformInitiative = Await.result(result, 1 second)
      finalResult shouldBe a [PlatformInitiative]
      finalResult.initiativeName shouldBe "Test"
    }
  }

  "allPlatformInitiatives" should {
    "return a future sequence of PlatformInitiatives" in new Setup {
      when(mockTeamsAndRepositoriesConnector.allDefaultBranches(any[HeaderCarrier])) thenReturn
        Future.successful(mockRepositories)
      when(mockServiceDependenciesConnector.getAllDependencies()(any[HeaderCarrier])) thenReturn
        Future.successful(mockDependencies)
      val result: Future[Seq[PlatformInitiative]] = platformInitiativesService.allPlatformInitiatives
      val finalResult: Seq[PlatformInitiative] = Await.result(result, 1 second)
      finalResult.length shouldBe 4
    }
  }

  private[this] trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockTeamsAndRepositoriesConnector: TeamsAndRepositoriesConnector = mock[TeamsAndRepositoriesConnector]
    val mockServiceDependenciesConnector: ServiceDependenciesConnector = mock[ServiceDependenciesConnector]
    val mockControllerComponents: ControllerComponents = mock[ControllerComponents]
    val platformInitiativesService: PlatformInitiativesService = new PlatformInitiativesService(mockTeamsAndRepositoriesConnector, mockServiceDependenciesConnector, mockControllerComponents)

    val mockRepositories: Seq[RepositoryDisplayDetails] = Seq(
      RepositoryDisplayDetails(
        name = "test",
        createdAt = LocalDateTime.now(),
        lastUpdatedAt = LocalDateTime.now(),
        isArchived = false,
        teamNames = Seq("Team-1", "Team-2"),
        defaultBranch = "main"
      ),
      RepositoryDisplayDetails(
        name = "test-2",
        createdAt = LocalDateTime.now(),
        lastUpdatedAt = LocalDateTime.now(),
        isArchived = false,
        teamNames = Seq("Team-1"),
        defaultBranch = "master"
      ),
      RepositoryDisplayDetails(
        name = "test-3",
        createdAt = LocalDateTime.now(),
        lastUpdatedAt = LocalDateTime.now(),
        isArchived = false,
        teamNames = Seq("Team-1"),
        defaultBranch = "main"
      ),
      RepositoryDisplayDetails(
        name = "test-4",
        createdAt = LocalDateTime.now(),
        lastUpdatedAt = LocalDateTime.now(),
        isArchived = true,
        teamNames = Seq("Team-2"),
        defaultBranch = "master"
      )
    )
    val mockDependencies = Seq(
      Dependencies(
        repositoryName = "repository1",
        libraryDependencies = Seq(
          Dependency(
            name = "Dep1",
            group = "Group1",
            currentVersion = Version(0, 2, 0),
            latestVersion = Option(Version(0, 2, 0))
          ),
          Dependency(
            name = "Dep2",
            group = "Group2",
            currentVersion = Version(1, 0, 1),
            latestVersion = Option(Version(1, 0, 1))
          )
        ),
        sbtPluginsDependencies = Seq(),
        otherDependencies = Seq()
      ),
      Dependencies(
        repositoryName = "repository2",
        libraryDependencies = Seq(
          Dependency(
            name = "Dep2",
            group = "Group2",
            currentVersion = Version(1, 0, 1),
            latestVersion = Option(Version(1, 0, 1))
          )
        ),
        sbtPluginsDependencies = Seq(),
        otherDependencies = Seq()
      ),
      Dependencies(
        repositoryName = "repository3",
        libraryDependencies = Seq(
          Dependency(
            name = "Dep1",
            group = "Group1",
            currentVersion = Version(1, 0, 1),
            latestVersion = Option(Version(1, 0, 1))
          )
        ),
        sbtPluginsDependencies = Seq(),
        otherDependencies = Seq()
      )
    )
  }
}
