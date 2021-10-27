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
import play.api.mvc.{ControllerComponents, Results}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.platforminitiatives.controllers.PlatformInitiativesController
import uk.gov.hmrc.platforminitiatives.models.{Dependencies, Dependency, Version}
import uk.gov.hmrc.platforminitiatives.services.PlatformInitiativesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.Success

class ServiceDependenciesConnectorSpec
  extends AnyWordSpec
    with Matchers
    with Results
    with MockitoSugar {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "getAllDependencies" should {
    "return a sequence of dependencies" in new Setup {
      when(mockServiceDependenciesConnector.getAllDependencies) thenReturn
        Future.successful(mockDependencies)

      val result: Future[Seq[Dependencies]] = mockServiceDependenciesConnector.getAllDependencies
      result.onComplete({
        case Success(value) => {
          value.length mustBe 1
        }
      })
    }
  }


  private[this] trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockTeamsAndRepositoriesConnector: TeamsAndRepositoriesConnector = mock[TeamsAndRepositoriesConnector]
    val mockServiceDependenciesConnector: ServiceDependenciesConnector = mock[ServiceDependenciesConnector]
    val mockControllerComponents: ControllerComponents = mock[ControllerComponents]
    val mockPlatformInitiativesService: PlatformInitiativesService = new PlatformInitiativesService(mockTeamsAndRepositoriesConnector, mockServiceDependenciesConnector, mockControllerComponents)

    val mockPlatformInitiativesController: PlatformInitiativesController = new PlatformInitiativesController(mockPlatformInitiativesService,mockControllerComponents)

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
      )
    )
  }

}
