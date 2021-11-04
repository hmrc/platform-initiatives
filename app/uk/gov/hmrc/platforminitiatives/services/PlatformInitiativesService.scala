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
import uk.gov.hmrc.platforminitiatives.connectors.{ServiceDependenciesConnector, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.platforminitiatives.models.{PlatformInitiative, Version}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PlatformInitiativesService @Inject()(
    teamsAndRepositoriesConnector : TeamsAndRepositoriesConnector,
    serviceDependenciesConnector  : ServiceDependenciesConnector,
    cc                            : ControllerComponents
  ) extends BackendController(cc) {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def allPlatformInitiatives(implicit ec: ExecutionContext): Future[Seq[PlatformInitiative]] = {
    val initiatives = Seq(
      createDefaultBranchInitiative(
        initiativeName        = "Update Default Branch Terminology",
        initiativeDescription = "To update default branch names - [Default branch tracker](https://catalogue.tax.service.gov.uk/defaultbranch).",
        completedLegend       = "Updated",
        inProgressLegend      = "Master"
      ),
      createUpgradeInitiative(
        initiativeName        = "Play 2.6 upgrade",
        initiativeDescription = "Play 2.6 upgrade - Deprecate [Play 2.5 and below](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=275944511).",
        dependencyName        = "play",
        version               = Option(Version(2,6,0))
      ),
      createUpgradeInitiative(
        initiativeName        = "Play 2.8 upgrade",
        initiativeDescription = "Play 2.8 upgrade - Deprecate [Play 2.7 and below](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=275944511).",
        dependencyName        = "play",
        version               = Option(Version(2,8,0))
      ),
      createUpgradeInitiative(
        initiativeName        = "Auth-client upgrade",
        initiativeDescription = "[CL250 Security upgrade required](https://confluence.tools.tax.service.gov.uk/x/RgpxDw)",
        dependencyName        = "auth-client",
        version               = Option(Version(5,6,0))
      )
    )
    Future.sequence(initiatives)
  }

  def createDefaultBranchInitiative(
     initiativeName              : String,
     initiativeDescription       : String,
     completedLegend             : String = "Completed",
     inProgressLegend            : String = "Not Completed",
   )(implicit ec: ExecutionContext): Future[PlatformInitiative] = {
    teamsAndRepositoriesConnector.allDefaultBranches.map { repos =>
      PlatformInitiative(
        initiativeName            = initiativeName,
        initiativeDescription     = initiativeDescription,
        currentProgress           = repos
          .filter   (!_.isArchived)
          .map      (_.defaultBranch)
          .count    (_ != "master"),
        targetProgress            = repos
          .filter   (!_.isArchived)
          .map      (_.defaultBranch)
          .length,
        completedLegend           = completedLegend,
        inProgressLegend          = inProgressLegend
      )
    }
  }

  def createUpgradeInitiative(
    initiativeName              : String,
    initiativeDescription       : String,
    dependencyName              : String,
    version                     : Option[Version],
    completedLegend             : String = "Completed",
    inProgressLegend            : String = "Not Completed",
  )(implicit ec: ExecutionContext): Future[PlatformInitiative] = {
      serviceDependenciesConnector.getAllDependencies.map { dependencies =>
        PlatformInitiative(
          initiativeName          = initiativeName,
          initiativeDescription   = initiativeDescription,
          currentProgress         = dependencies
            .flatten(_.toDependencySeq
              .filter(_.name == dependencyName)
              .filter(repo => version.fold(false)(repo.currentVersion >= _))
            ).length,
          targetProgress          = dependencies
            .flatten(_.toDependencySeq)
            .count(_.name == dependencyName),
          completedLegend         = completedLegend,
          inProgressLegend        = inProgressLegend
        )
      }
    }
}
