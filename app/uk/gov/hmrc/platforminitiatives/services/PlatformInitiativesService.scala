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

import play.api.Configuration
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.platforminitiatives.connectors.{ServiceDependenciesConnector, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.platforminitiatives.models.{Environment, PlatformInitiative, Progress, Version}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.http.StringContextOps
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PlatformInitiativesService @Inject()(
    configuration                 : Configuration,
    teamsAndRepositoriesConnector : TeamsAndRepositoriesConnector,
    serviceDependenciesConnector  : ServiceDependenciesConnector,
    cc                            : ControllerComponents
  ) extends BackendController(cc) {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  val displayExperimentalInitiatives: Boolean = configuration.get[Boolean]("initiatives.service.includeExperimental")

  def allPlatformInitiatives(team: Option[String] = None)(implicit ec: ExecutionContext): Future[Seq[PlatformInitiative]] = {
    val teamName = team.getOrElse("")

    val initiatives = Seq(
      createDefaultBranchInitiative(
        initiativeName        = "Update Default Branch Terminology",
        team                  = team,
        initiativeDescription = "To update default branch names - [Default Branch Tracker](" + url"https://catalogue.tax.service.gov.uk/defaultbranch?name=&teamNames=$teamName&defaultBranch=master" + ") | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/display/TEC/2021/10/08/Default Branch Migration: How To" + ").",
        completedLegend       = "Updated",
        inProgressLegend      = "Master"
      ),
      createMigrationInitiative(
        initiativeName        = "Play 3.0 upgrade - Production",
        initiativeDescription = "Play 3.0 upgrade - Deprecate [Play 2.9 and below](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=com.typesafe.play&artefact=play&team=$teamName&flag=production&scope[]=compile&versionRange=[0.0.0,3.0.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=774373449" + ").",
        newGroup              = "org.playframework",
        newArtefact           = "play",
        oldGroup              = "com.typesafe.play",
        oldArtefact           = "play",
        team                  = team
      ),
      createMigrationInitiative(
        initiativeName        = "Play 3.0 upgrade - Latest",
        initiativeDescription = "Play 3.0 upgrade - Deprecate [Play 2.9 and below](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=com.typesafe.play&artefact=play&team=$teamName&flag=latest&scope[]=compile&versionRange=[0.0.0,3.0.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=774373449" + ").",
        newGroup              = "org.playframework",
        newArtefact           = "play",
        oldGroup              = "com.typesafe.play",
        oldArtefact           = "play",
        team                  = team,
        environment           = None
      ),
      createUpgradeInitiative(
        initiativeName        = "Auth-client upgrade",
        initiativeDescription = "[CL250 Security upgrade required](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=uk.gov.hmrc&artefact=auth-client&team=$teamName&flag=production&scope[]=compile&versionRange=[0.0.0,5.6.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/x/RgpxDw" + ").",
        group                 = "uk.gov.hmrc",
        artefact              = "auth-client",
        version               = Version(5,6,0,"5.6.0"),
        team                  = team
      ),
      createUpgradeInitiative(
        initiativeName        = "Scala 2.13 Upgrade",
        initiativeDescription = "Scala 2.13 upgrade - Deprecate [Scala 2.12 and below](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=org.scala-lang&artefact=scala-library&team=$teamName&flag=production&scope[]=compile&versionRange=[0.0.0,2.13.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=275944511" + ").",
        group                 = "org.scala-lang",
        artefact              = "scala-library",
        version               = Version(2,13,0,"2.13.0"),
        team                  = team,
      ),
      createMigrationInitiative(
        initiativeName        = "Replace simple-reactivemongo with hmrc-mongo",
        initiativeDescription = "Monitoring [repos still using simple-reactivemongo](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=uk.gov.hmrc&artefact=simple-reactivemongo&team=$teamName&flag=production&scope[]=compile&versionRange=[0.0.0,99.0.0)&asCsv=false".toString.replace(")", "\\)") + " ) and [repos now using hmrc-mongo](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=uk.gov.hmrc.mongo&artefact=hmrc-mongo-common&team=$teamName&flag=production&scope[]=compile&versionRange=[0.0.0,99.0.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/display/TEC/2021/03/04/HMRC+Mongo+is+now+available" + ").",
        newGroup              = "uk.gov.hmrc.mongo",
        newArtefact           = "hmrc-mongo-common",
        oldGroup              = "uk.gov.hmrc",
        oldArtefact           = "simple-reactivemongo",
        team                  = team,
        inProgressLegend      = "Simple-Reactivemongo",
        completedLegend       = "HMRC-Mongo"
      ),
      createJavaInitiative(
        initiativeName        = "Java 11 Upgrade",
        initiativeDescription = s"""[Java 11 upgrade](${url"https://catalogue.tax.service.gov.uk/jdkexplorer/latest?teamName=$teamName"}) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/x/TwLIH"})""",
        version               = Version.apply("11.0.0"),
        team                  = team
      )
    )

    Future.traverse(initiatives)(_.filter(_.progress.target != 0).filter(!_.experimental || displayExperimentalInitiatives))
  }

  def createDefaultBranchInitiative(
     initiativeName       : String,
     initiativeDescription: String,
     team                 : Option[String] = None,
     completedLegend      : String         = "Completed",
     inProgressLegend     : String         = "Not Completed",
     experimental         : Boolean        = false
   )(implicit
     ec                   : ExecutionContext
   ): Future[PlatformInitiative] =
    teamsAndRepositoriesConnector
      .allDefaultBranches()
      .map { repos =>
        PlatformInitiative(
          initiativeName        = initiativeName,
          initiativeDescription = initiativeDescription,
          progress              = Progress(
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
          completedLegend       = completedLegend,
          inProgressLegend      = inProgressLegend,
          experimental          = experimental
        )
      }

  def createJavaInitiative(
     initiativeName       : String,
     initiativeDescription: String,
     version              : Version,
     team                 : Option[String] = None,
     completedLegend      : String         = "Completed",
     inProgressLegend     : String         = "Not Completed",
     experimental         : Boolean        = false
   )(implicit
     ec: ExecutionContext
   ): Future[PlatformInitiative] =
    serviceDependenciesConnector
      .getSlugJdkVersions(team)
      .map { repos =>
        PlatformInitiative(
          initiativeName            = initiativeName,
          initiativeDescription     = initiativeDescription,
          progress                  = Progress(
                                        current = repos.count(_.version >= version),
                                        target  = repos.length
                                      ),
          completedLegend           = completedLegend,
          inProgressLegend          = inProgressLegend,
          experimental              = experimental
        )
      }

  def createUpgradeInitiative(
    initiativeName       : String,
    initiativeDescription: String,
    group                : String,
    artefact             : String,
    version              : Version,
    team                 : Option[String] = None,
    environment          : Option[Environment] = Some(Environment.Production),
    completedLegend      : String = "Completed",
    inProgressLegend     : String = "Not Completed",
    experimental         : Boolean = false
  )(implicit
    ec                   : ExecutionContext
  ): Future[PlatformInitiative] =
    serviceDependenciesConnector
      .getServiceDependency(group, artefact, environment)
      .map(sd => team.fold(sd)(t => sd.filter(_.teams == Seq(t)))) // Filtering for exclusively owned repos, if set
      .map { dependencies =>
        PlatformInitiative(
          initiativeName          = initiativeName,
          initiativeDescription   = initiativeDescription,
          progress                = Progress(
                                      current = dependencies.count(d => Version(d.depVersion) >= version),
                                      target  = dependencies.length
                                    ),
          completedLegend         = completedLegend,
          inProgressLegend        = inProgressLegend,
          experimental            = experimental
        )
      }

  def createMigrationInitiative(
   initiativeName       : String,
   initiativeDescription: String,
   newGroup             : String,
   newArtefact          : String,
   oldGroup             : String,
   oldArtefact          : String,
   team                 : Option[String]      = None,
   environment          : Option[Environment] = Some(Environment.Production),
   completedLegend      : String              = "Completed",
   inProgressLegend     : String              = "Not Completed",
   experimental         : Boolean             = false
 )(implicit
   ec                   : ExecutionContext
 ): Future[PlatformInitiative] =
    for {
      firstArtefactDependencies   <- serviceDependenciesConnector.getServiceDependency(newGroup, newArtefact, environment)
      secondArtefactDependencies  <- serviceDependenciesConnector.getServiceDependency(oldGroup, oldArtefact, environment)
      allDependencies             =  (firstArtefactDependencies ++ secondArtefactDependencies)
                                       // Filtering for exclusively owned repos
                                       .filter(dependencies => team.fold(true)(dependencies.teams == Seq(_)))
    } yield PlatformInitiative(
      initiativeName              =  initiativeName,
      initiativeDescription       =  initiativeDescription,
      progress                    =  Progress(
                                       current = allDependencies
                                                   .count(x => x.depArtefact == newArtefact && x.depGroup == newGroup),
                                       target  = allDependencies.length
                                     ),
      completedLegend             =  completedLegend,
      inProgressLegend            =  inProgressLegend,
      experimental                =  experimental
    )
}
