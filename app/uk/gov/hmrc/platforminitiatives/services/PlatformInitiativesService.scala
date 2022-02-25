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

import play.api.mvc.ControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.platforminitiatives.connectors.{ServiceDependenciesConnector, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.platforminitiatives.models.{Environment, PlatformInitiative, Progress, Version}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.http.StringContextOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PlatformInitiativesService @Inject()(
    teamsAndRepositoriesConnector : TeamsAndRepositoriesConnector,
    serviceDependenciesConnector  : ServiceDependenciesConnector,
    cc                            : ControllerComponents
  ) extends BackendController(cc) {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def allPlatformInitiatives(team: Option[String] = None)(implicit ec: ExecutionContext): Future[Seq[PlatformInitiative]] = {
    val teamName = team match {
      case None => ""
      case Some(team) => team
    }
    val initiatives = Seq(
      createDefaultBranchInitiative(
        initiativeName        = "Update Default Branch Terminology",
        team                  = team,
        initiativeDescription = s"To update default branch names - [Default Branch Tracker](" + url"https://catalogue.tax.service.gov.uk/defaultbranch?name=&teamNames=$teamName&defaultBranch=master" + ") | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/display/TEC/2021/10/08/Default Branch Migration: How To" + ").",
        completedLegend       = "Updated",
        inProgressLegend      = "Master"
      ),
      createUpgradeInitiative(
        initiativeName        = "Play 2.6 upgrade",
        initiativeDescription = s"Play 2.6 upgrade - Deprecate [Play 2.5 and below](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=com.typesafe.play&artefact=play&team=$teamName&flag=production&scope=compile&versionRange=[0.0.0,2.6.0)&asCsv=false".toString.replace(")", "\\)") + ") | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=275944511" + ").",
        group                 = "com.typesafe.play",
        artefact              = "play",
        version               = Version(2,6,0,"2.6.0"),
        team                  = team
      ),
      createUpgradeInitiative(
        initiativeName        = "Play 2.8 upgrade - Production",
        initiativeDescription = s"Play 2.8 upgrade - Deprecate [Play 2.7 and below](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=com.typesafe.play&artefact=play&team=$teamName&flag=production&scope=compile&versionRange=[0.0.0,2.8.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=275944511" + ").",
        group                 = "com.typesafe.play",
        artefact              = "play",
        version               = Version(2,8,0,"2.8.0"),
        team                  = team
      ),
      createUpgradeInitiative(
        initiativeName        = "Play 2.8 upgrade - Latest",
        initiativeDescription = s"Play 2.8 upgrade - Deprecate [Play 2.7 and below](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=com.typesafe.play&artefact=play&team=$teamName&flag=latest&scope=compile&versionRange=[0.0.0,2.8.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=275944511" + ").",
        group                 = "com.typesafe.play",
        artefact              = "play",
        version               = Version(2,8,0,"2.8.0"),
        team                  = team,
        environment           = None
      ),
      createUpgradeInitiative(
        initiativeName        = "Auth-client upgrade",
        initiativeDescription = s"[CL250 Security upgrade required](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=uk.gov.hmrc&artefact=auth-client&team=$teamName&flag=production&scope=compile&versionRange=[0.0.0,5.6.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/x/RgpxDw" + ").",
        group                 = "uk.gov.hmrc",
        artefact              = "auth-client",
        version               = Version(5,6,0,"5.6.0"),
        team                  = team
      )
    )
    Future.sequence(initiatives).map(_.filter(_.progress.target != 0))
  }

  def createDefaultBranchInitiative(
     initiativeName              : String,
     initiativeDescription       : String,
     team                        : Option[String] = None,
     completedLegend             : String = "Completed",
     inProgressLegend            : String = "Not Completed",
   )(implicit ec: ExecutionContext): Future[PlatformInitiative] = {
    teamsAndRepositoriesConnector.allDefaultBranches.map { repos =>
      PlatformInitiative(
        initiativeName            = initiativeName,
        initiativeDescription     = initiativeDescription,
        progress                  = Progress(
          current = repos
            // Filtering for exclusively owned repos
            .filter(repositories => team.fold(true)(repositories.teamNames == Seq(_)))
            .filter(!_.isArchived)
            .map(_.defaultBranch)
            .count(_ != "master"),
          target = repos
            .filter(repositories => team.fold(true)(repositories.teamNames == Seq(_)))
            .filter(!_.isArchived)
            .map(_.defaultBranch)
            .length
        ),
        completedLegend           = completedLegend,
        inProgressLegend          = inProgressLegend
      )
    }
  }

  def createUpgradeInitiative(
    initiativeName              : String,
    initiativeDescription       : String,
    group                       : String,
    artefact                    : String,
    version                     : Version,
    team                        : Option[String] = None,
    environment                 : Option[Environment] = Some(Environment.Production),
    completedLegend             : String = "Completed",
    inProgressLegend            : String = "Not Completed"
  )(implicit ec: ExecutionContext): Future[PlatformInitiative] = {
    serviceDependenciesConnector.getServiceDependency(group, artefact, environment)
      .map { dependencies =>
        PlatformInitiative(
          initiativeName          = initiativeName,
          initiativeDescription   = initiativeDescription,
          progress                = Progress(
            current = dependencies
              // Filtering for exclusively owned repos
              .filter(dependencies => team.fold(true)(dependencies.teams == Seq(_)))
              .count(_.depVersion >= version.original),
            target  = dependencies
              .count(dependencies => team.fold(true)(dependencies.teams == Seq(_)))
          ),
          completedLegend         = completedLegend,
          inProgressLegend        = inProgressLegend
        )
      }
    }
}
