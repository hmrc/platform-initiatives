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

import cats.implicits._
import play.api.Configuration
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.platforminitiatives.connectors.{ServiceDependenciesConnector, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.platforminitiatives.models.DependencyScope.{Compile, Test}
import uk.gov.hmrc.platforminitiatives.models._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PlatformInitiativesService @Inject()(
  configuration                 : Configuration,
  teamsAndRepositoriesConnector : TeamsAndRepositoriesConnector,
  serviceDependenciesConnector  : ServiceDependenciesConnector,
  cc                            : ControllerComponents
) extends BackendController(cc) {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val displayExperimentalInitiatives: Boolean =
    configuration.get[Boolean]("initiatives.service.includeExperimental")

  def allPlatformInitiatives(teamName: Option[String] = None)(implicit ec: ExecutionContext): Future[Seq[PlatformInitiative]] =
    List(
      createMigrationInitiative(
        initiativeName        = "Migration to new UI test tooling",
        initiativeDescription = "Migration from [webdriver-factory](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=uk.gov.hmrc&artefact=webdriver-factory&versionRange=[0.0.0,)&asCsv=false&team=$teamName&flag=latest&repoType[]=Service&repoType[]=Library&repoType[]=Test&repoType[]=Other&scope[]=test".toString.replace(")", "\\)") + " ) to [ui-test-runner](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=uk.gov.hmrc&artefact=ui-test-runner&versionRange=[0.0.0,)&asCsv=false&team=$teamName&flag=latest&repoType[]=Service&repoType[]=Library&repoType[]=Test&repoType[]=Other&scope[]=test".toString.replace(")", "\\)") + " )  | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=804554026" + ").",
        fromArtefacts         = Seq(Artefact("uk.gov.hmrc","webdriver-factory")),
        toArtefacts           = Seq(Artefact("uk.gov.hmrc", "ui-test-runner")),
        team                  = teamName,
        environment           = None,
        scopes                = List(Test)
      ),
      createMigrationInitiative(
        initiativeName        = "Tudor Crown Upgrade - Production",
        initiativeDescription = "Monitoring repos still using [play-frontend-hmrc](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=uk.gov.hmrc&artefact=play-frontend-hmrc&team=$teamName&flag=production&versionRange=[0.0.0,)&asCsv=false".toString.replace(")", "\\)") + " ) or below v8.5.0 of [play-frontend-hmrc-play-28](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=uk.gov.hmrc&artefact=play-frontend-hmrc-play-28&team=$teamName&flag=production&versionRange=[0.0.0,8.5.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [29](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=uk.gov.hmrc&artefact=play-frontend-hmrc-play-29&team=$teamName&flag=production&versionRange=[0.0.0,8.5.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [30](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=uk.gov.hmrc&artefact=play-frontend-hmrc-play-30&team=$teamName&flag=production&versionRange=[0.0.0,8.5.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=815170354" + ").",
        fromArtefacts         = Seq(Artefact("uk.gov.hmrc","play-frontend-hmrc")),
        toArtefacts           = Seq(Artefact("uk.gov.hmrc", "play-frontend-hmrc-play-28"), Artefact("uk.gov.hmrc", "play-frontend-hmrc-play-29"), Artefact("uk.gov.hmrc", "play-frontend-hmrc-play-30")),
        targetVersion         = Some(Version(8, 5, 0, "8.5.0")),
        team                  = teamName
      ),
      createDefaultBranchInitiative(
        initiativeName        = "Update Default Branch Terminology",
        team                  = teamName,
        initiativeDescription = "To update default branch names - [Default Branch Tracker](" + url"https://catalogue.tax.service.gov.uk/defaultbranch?name=&teamNames=$teamName&defaultBranch=master" + ") | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/display/TEC/2021/10/08/Default Branch Migration: How To" + ").",
        completedLegend       = "Updated",
        inProgressLegend      = "Master"
      ),
      createMigrationInitiative(
        initiativeName        = "Play 3.0 upgrade - Production",
        initiativeDescription = "Play 3.0 upgrade - Deprecate [Play 2.9 and below](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=com.typesafe.play&artefact=play&team=$teamName&flag=production&versionRange=[0.0.0,3.0.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=774373449" + ").",
        fromArtefacts         = Seq(Artefact("com.typesafe.play", "play")),
        toArtefacts           = Seq(Artefact("org.playframework", "play")),
        team                  = teamName
      ),
      createMigrationInitiative(
        initiativeName        = "Play 3.0 upgrade - Latest",
        initiativeDescription = "Play 3.0 upgrade - Deprecate [Play 2.9 and below](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=com.typesafe.play&artefact=play&team=$teamName&repoType[]=Service&repoType[]=Library&repoType[]=Prototype&repoType[]=Test&repoType[]=Other&versionRange=[0.0.0,3.0.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=774373449" + ").",
        fromArtefacts         = Seq(Artefact("com.typesafe.play", "play")),
        toArtefacts           = Seq(Artefact("org.playframework", "play")),
        team                  = teamName,
        environment           = None
      ),
      createUpgradeInitiative(
        initiativeName        = "Auth-client upgrade",
        initiativeDescription = "[CL250 Security upgrade required](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=uk.gov.hmrc&artefact=auth-client&team=$teamName&flag=production&versionRange=[0.0.0,5.6.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/x/RgpxDw" + ").",
        group                 = "uk.gov.hmrc",
        artefact              = "auth-client",
        version               = Version(5,6,0,"5.6.0"),
        team                  = teamName
      ),
      createUpgradeInitiative(
        initiativeName        = "Scala 2.13 Upgrade",
        initiativeDescription = "Scala 2.13 upgrade - Deprecate [Scala 2.12 and below](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=org.scala-lang&artefact=scala-library&team=$teamName&flag=production&versionRange=[0.0.0,2.13.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=275944511" + ").",
        group                 = "org.scala-lang",
        artefact              = "scala-library",
        version               = Version(2,13,0,"2.13.0"),
        team                  = teamName,
      ),
      createMigrationInitiative(
        initiativeName        = "Replace simple-reactivemongo with hmrc-mongo",
        initiativeDescription = "Monitoring [repos still using simple-reactivemongo](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=uk.gov.hmrc&artefact=simple-reactivemongo&team=$teamName&flag=production&versionRange=[0.0.0,99.0.0)&asCsv=false".toString.replace(")", "\\)") + " ) and [repos now using hmrc-mongo](" + url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=uk.gov.hmrc.mongo&artefact=hmrc-mongo-common&team=$teamName&flag=production&versionRange=[0.0.0,99.0.0)&asCsv=false".toString.replace(")", "\\)") + " ) | [Confluence](" + url"https://confluence.tools.tax.service.gov.uk/display/TEC/2021/03/04/HMRC+Mongo+is+now+available" + ").",
        fromArtefacts         = Seq(Artefact("uk.gov.hmrc", "simple-reactivemongo")),
        toArtefacts           = Seq(Artefact("uk.gov.hmrc.mongo", "hmrc-mongo-common")),
        team                  = teamName,
        inProgressLegend      = "Simple-Reactivemongo",
        completedLegend       = "HMRC-Mongo"
      ),
      createJavaInitiative(
        initiativeName        = "Java 11 Upgrade",
        initiativeDescription = s"""[Java 11 upgrade](${url"https://catalogue.tax.service.gov.uk/jdkexplorer/latest?teamName=$teamName"}) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/x/TwLIH"})""",
        version               = Version.apply("11.0.0"),
        team                  = teamName
      ),
      createJavaInitiative(
        initiativeName        = "Java 21 Upgrade",
        initiativeDescription = s"""[Java 21 upgrade](${url"https://catalogue.tax.service.gov.uk/jdkexplorer/latest?teamName=$teamName"}) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/x/xIACM"})""",
        version               = Version.apply("21.0.0"),
        team                  = teamName
      )
    ).sequence
     .map(
       _.filter(_.progress.target != 0)
        .filter(!_.experimental || displayExperimentalInitiatives)
     )

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
    team                 : Option[String]        = None,
    environment          : Option[Environment]   = Some(Environment.Production),
    scopes               : List[DependencyScope] = List(Compile),
    completedLegend      : String                = "Completed",
    inProgressLegend     : String                = "Not Completed",
    experimental         : Boolean               = false
  )(implicit
    ec                   : ExecutionContext
  ): Future[PlatformInitiative] =
    serviceDependenciesConnector
      .getMetaArtefactDependency(group, artefact, environment, scopes)
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
    fromArtefacts        : Seq[Artefact],
    toArtefacts          : Seq[Artefact],
    targetVersion        : Option[Version]       = None,
    team                 : Option[String]        = None,
    environment          : Option[Environment]   = Some(Environment.Production),
    scopes               : List[DependencyScope] = List(Compile),
    completedLegend      : String                = "Completed",
    inProgressLegend     : String                = "Not Completed",
    experimental         : Boolean               = false
  )(implicit
    ec                   : ExecutionContext
  ): Future[PlatformInitiative] =
    for {
      fromDependencies     <- fromArtefacts
                                .traverse(a => serviceDependenciesConnector.getMetaArtefactDependency(a.group, a.name, environment, scopes))
                                .map(_.flatten)
                                .map(_.filter(dependencies => team.fold(true)(dependencies.teams == Seq(_)))) // Filtering for exclusively owned repos
      targetDependencies   <- toArtefacts
                                .traverse(a => serviceDependenciesConnector.getMetaArtefactDependency(a.group, a.name, environment, scopes))
                                .map(_.flatten)
                                .map(_.filter(dependencies => team.fold(true)(dependencies.teams == Seq(_)))) // Filtering for exclusively owned repos
      allDependencies       = (fromDependencies ++ targetDependencies)
    } yield PlatformInitiative(
      initiativeName        = initiativeName,
      initiativeDescription = initiativeDescription,
      progress              = Progress(
                                current = targetVersion.fold(targetDependencies.length)(v => targetDependencies.count(d => Version(d.depVersion) >= v)),
                                target  = allDependencies.length
                              ),
      completedLegend       = completedLegend,
      inProgressLegend      = inProgressLegend,
      experimental          = experimental
    )
}
