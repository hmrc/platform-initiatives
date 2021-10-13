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

package uk.gov.hmrc.platforminitiatives.controllers

import play.api.libs.json.Json
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.platforminitiatives.connectors.{ServiceDependenciesConnector, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.platforminitiatives.models.Version

import javax.inject.{Inject, Singleton}
import scala.concurrent.{Await, ExecutionContext}
import uk.gov.hmrc.platforminitiatives.services.PlatformInitiativesService

import scala.concurrent.duration.DurationInt

@Singleton
class PlatformInitiativesController @Inject()(
   platformInitiativesService       :   PlatformInitiativesService,
   teamsAndRepositoriesConnector    :   TeamsAndRepositoriesConnector,
   serviceDependenciesConnector     :   ServiceDependenciesConnector,
   cc                               :   ControllerComponents
 )(implicit val ec: ExecutionContext)
  extends BackendController(cc) {

  implicit val hc = HeaderCarrier()

  def displayDefaultBranchInitiative() = Action.async {
    teamsAndRepositoriesConnector.allDefaultBranches.map { repositories =>
      Ok(Json.toJson(repositories))
    }
  }

  def tryingThisOut: Action[AnyContent] = Action.async { implicit request =>
    platformInitiativesService.getAllFutureDefaultBranches.map { repository =>

//      return futures here?

//      defaultBranchInitiative = platformInitiativesService.transformDefaultBranches(
//        initiativeName = "Hello",
//        initiativeDescription = "Goodbye!",
//        currentProgress = platformInitiativesService.getAllFutureDefaultBranches.map(_.count(_.defaultBranch != "master"))
//        targetProgress = platformInitiativesService.getAllFutureDefaultBranches.map(_.count(_.defaultBranch == "master")),
//      )

      Ok(Json.toJson(repository))
    }
  }

  def displayInitiatives: Action[AnyContent] = Action {
    val defaultBranches = platformInitiativesService.getAllDefaultBranches
    val dependencies = platformInitiativesService.getAllServiceDependencies

    val defaultBranchInitiative = platformInitiativesService.transformDefaultBranches(
      initiativeName = "Update Default Branch Terminology",
      initiativeDescription = "To eliminate controversial terminology from our repositories.",
      currentProgress = defaultBranches.count(_.defaultBranch != "master"),
      targetProgress = defaultBranches.length,
      completedLegend = "Updated",
      inProgressLegend = "Master"
      )
    val upgradePlay26Initiative = platformInitiativesService.transformDefaultBranches(
      initiativeName = "Play 2.6 upgrade",
      initiativeDescription = "Play 2.6 upgrade - Deprecate [Play 2.5 and below](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=275944511).",
      currentProgress = dependencies.flatten(_.libraryDependencies
        .filter(_.name == "play")
        .filter(_.currentVersion.minor >= 6)
      ).length,
      targetProgress = dependencies.flatten(_.libraryDependencies.filter(_.name == "play")).length
    )
    val upgradePlay27Initiative = platformInitiativesService.transformDefaultBranches(
      initiativeName = "Play 2.7 upgrade",
      initiativeDescription = "Play 2.7 upgrade - Deprecate [Play 2.6 and below](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=275944511).",
      currentProgress = dependencies.flatten(_.libraryDependencies
        .filter(_.name == "play")
        .filter(_.currentVersion.minor >= 7)
      ).length,
      targetProgress = dependencies.flatten(_.libraryDependencies.filter(_.name == "play")).length
    )
    val upgradePlay28Initiative = platformInitiativesService.transformDefaultBranches(
      initiativeName = "Play 2.8 upgrade",
      initiativeDescription = "Play 2.8 upgrade - Deprecate [Play 2.7 and below](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=275944511).",
      currentProgress = dependencies.flatten(_.libraryDependencies
        .filter(_.name == "play")
        .filter(_.currentVersion.minor >= 8)
      ).length,
      targetProgress = dependencies.flatten(_.libraryDependencies.filter(_.name == "play")).length
    )
    val upgradeAuthClientInitiative = platformInitiativesService.transformDefaultBranches(
      initiativeName = "Auth-client upgrade",
      initiativeDescription = "[CL250 Security upgrade required](https://confluence.tools.tax.service.gov.uk/x/RgpxDw)",
      currentProgress = dependencies.flatten(_.libraryDependencies
        .filter(_.name == "auth-client")
        .filter(_.currentVersion < Version(5,6,0,"5.6.0"))
      ).length,
      targetProgress = dependencies.flatten(_.libraryDependencies.filter(_.name == "auth-client")).length
    )

    val allInitiatives = Seq(
      defaultBranchInitiative,
      upgradePlay26Initiative,
      upgradePlay27Initiative,
      upgradePlay28Initiative,
      upgradeAuthClientInitiative
    )
    Ok(Json.toJson(allInitiatives))
    }
  }
