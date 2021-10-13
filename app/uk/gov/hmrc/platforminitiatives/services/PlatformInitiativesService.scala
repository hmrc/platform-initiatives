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

import play.api.mvc.ControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.platforminitiatives.connectors.{RepositoryDisplayDetails, ServiceDependenciesConnector, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.platforminitiatives.models.{Dependencies, PlatformInitiative}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.net.URL
import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class PlatformInitiativesService @Inject()(
    teamsAndRepositoriesConnector: TeamsAndRepositoriesConnector,
    serviceDependenciesConnector: ServiceDependenciesConnector,
    cc:                            ControllerComponents
  ) extends BackendController(cc) {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  // TODO: Improve this - This needs to be a for loop over future values.
  //        This will work for the purposes of demonstrating.
  def getAllDefaultBranches(implicit ec: ExecutionContext): Seq[RepositoryDisplayDetails] = {
    Await.result(teamsAndRepositoriesConnector.allDefaultBranches, Duration.create("1 second"))
  }
  def getAllFutureDefaultBranches(implicit ec: ExecutionContext): Future[Seq[RepositoryDisplayDetails]] = {
    teamsAndRepositoriesConnector.allDefaultBranches.map { repositories =>
      repositories
    }
  }

  def getAllServiceDependencies(implicit ec: ExecutionContext): Seq[Dependencies] = {
    Await.result(serviceDependenciesConnector.getAllDependencies(), Duration.create("5 seconds"))
  }

  def transformDefaultBranches(
    initiativeName            : String,
    initiativeDescription     : String,
    currentProgress           : Int,
    targetProgress            : Int,
    completedLegend           : String = "Completed",
    inProgressLegend          : String = "In progress",
  ): PlatformInitiative = {
    PlatformInitiative(
      initiativeName          =     initiativeName,
      initiativeDescription   =     initiativeDescription,
      currentProgress         =     currentProgress,
      targetProgress          =     targetProgress,
      completedLegend         =     completedLegend,
      inProgressLegend        =     inProgressLegend
    )
  }
}
