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

import org.mockito.MockitoSugar
import org.mockito.ArgumentMatchersSugar
import org.mockito.ArgumentMatchers.anyString
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.platforminitiatives.connectors.{RepositoryDisplayDetails, ServiceDependenciesConnector, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.platforminitiatives.models.{PlatformInitiative, Progress, SlugDependencies, Version}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

class PlatformInitiativesServiceSpec
  extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ArgumentMatchersSugar
    with ScalaFutures
    with IntegrationPatience
  {

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
      finalResult.progress shouldBe Progress(2,3)
    }
  }

  "createUpgradeInitiative" should {
    "return an initiative for a Dependency Upgrade" in new Setup {
      when(mockServiceDependenciesConnector.getServiceDependency(
        group       = anyString,
        artefact    = anyString,
        environment = any,
        range       = anyString)(any[HeaderCarrier])) thenReturn
        Future.successful(mockSlugDependencies)
      val result: Future[PlatformInitiative] = platformInitiativesService.createUpgradeInitiative(
        initiativeName        = "Test",
        initiativeDescription = "Test Description",
        group                 = "uk.gov.hmrc",
        artefact              = "Dep1",
        version               = Version(0, 2, 0)
      )
      val finalResult: PlatformInitiative = result.futureValue
      finalResult shouldBe a [PlatformInitiative]
      finalResult.initiativeName shouldBe "Test"
      finalResult.progress shouldBe Progress(2,2)
    }
  }

  "allPlatformInitiatives" should {
    "return a future sequence of PlatformInitiatives" in new Setup {
      when(mockConfiguration.get[Boolean]("initiatives.service.includeExperimental")) thenReturn true
      when(mockTeamsAndRepositoriesConnector.allDefaultBranches(any[HeaderCarrier])) thenReturn
        Future.successful(mockRepositories)
      when(mockServiceDependenciesConnector.getServiceDependency(
        group       = anyString,
        artefact    = anyString,
        environment = any,
        range       = anyString)(any[HeaderCarrier])) thenReturn
          Future.successful(mockSlugDependencies)
      val result: Future[Seq[PlatformInitiative]] = platformInitiativesService.allPlatformInitiatives()
      val finalResult: Seq[PlatformInitiative] = result.futureValue
      finalResult.length shouldBe 5
    }
  }

  private[this] trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockConfiguration: Configuration = mock[Configuration]
    val mockTeamsAndRepositoriesConnector: TeamsAndRepositoriesConnector = mock[TeamsAndRepositoriesConnector]
    val mockServiceDependenciesConnector: ServiceDependenciesConnector = mock[ServiceDependenciesConnector]
    val mockControllerComponents: ControllerComponents = mock[ControllerComponents]
    val platformInitiativesService: PlatformInitiativesService = new PlatformInitiativesService(mockConfiguration, mockTeamsAndRepositoriesConnector, mockServiceDependenciesConnector, mockControllerComponents)

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
  }
}
