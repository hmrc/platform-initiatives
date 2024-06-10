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
        initiativeDescription = s"""Migration from [webdriver-factory](${
                                  dependencyExplorerUrl(
                                    group     = "uk.gov.hmrc",
                                    artefact  = "webdriver-factory",
                                    team      = teamName,
                                    repoTypes = Seq("Service", "Library", "Test", "Other"),
                                    scopes    = Seq("test")
                                  )
                                 } ) to [ui-test-runner](${
                                  dependencyExplorerUrl(
                                    group     = "uk.gov.hmrc",
                                    artefact  = "ui-test-runner",
                                    team      = teamName,
                                    repoTypes = Seq("Service", "Library", "Test", "Other"),
                                    scopes    = Seq("test")
                                  )
                                })  | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=804554026"}).""",
        fromArtefacts         = Seq(Artefact("uk.gov.hmrc","webdriver-factory")),
        toArtefacts           = Seq(Artefact("uk.gov.hmrc", "ui-test-runner")),
        team                  = teamName,
        environment           = None,
        scopes                = List(Test)
      ),
      createMigrationInitiative(
        initiativeName        = "Tudor Crown Upgrade - Production",
        initiativeDescription = s"""Monitoring repos still using [play-frontend-hmrc](${
                                  dependencyExplorerUrl(
                                    group    = "uk.gov.hmrc",
                                    artefact = "play-frontend-hmrc",
                                    flag     = "production",
                                    team     = teamName
                                  )
                                }) or below v8.5.0 of [play-frontend-hmrc-play-28](${
                                  dependencyExplorerUrl(
                                    group        = "uk.gov.hmrc",
                                    artefact     = "play-frontend-hmrc-play-28",
                                    team         = teamName,
                                    flag         = "production",
                                    versionRange = Some("[0.0.0,8.5.0)")
                                  )
                                }) | [29](${
                                  dependencyExplorerUrl(
                                    group        = "uk.gov.hmrc",
                                    artefact     = "play-frontend-hmrc-play-29",
                                    flag         = "production",
                                    team         = teamName,
                                    versionRange = Some("[0.0.0,8.5.0)")
                                  )
                                } ) | [30](${
                                  dependencyExplorerUrl(
                                   group        = "uk.gov.hmrc",
                                   artefact     = "play-frontend-hmrc-play-30",
                                   team         = teamName,
                                   flag         = "production",
                                   versionRange = Some("[0.0.0,8.5.0)")
                                  )
                                } ) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=815170354"}).""",
        fromArtefacts         = Seq(Artefact("uk.gov.hmrc","play-frontend-hmrc")),
        toArtefacts           = Seq(Artefact("uk.gov.hmrc", "play-frontend-hmrc-play-28"), Artefact("uk.gov.hmrc", "play-frontend-hmrc-play-29"), Artefact("uk.gov.hmrc", "play-frontend-hmrc-play-30")),
        targetVersion         = Some(Version("8.5.0")),
        team                  = teamName
      ),
      createDefaultBranchInitiative(
        initiativeName        = "Update Default Branch Terminology",
        team                  = teamName,
        initiativeDescription = s"""To update default branch names - [Default Branch Tracker](${
                                  url"https://catalogue.tax.service.gov.uk/defaultbranch?name=&teamNames=$teamName&defaultBranch=master"
                                }) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/display/TEC/2021/10/08/Default Branch Migration: How To"}).""",
        completedLegend       = "Updated",
        inProgressLegend      = "Master"
      ),
      createMigrationInitiative(
        initiativeName        = "Scala 3 Upgrade",
        initiativeDescription = s"""Scala 3 upgrade [repos still using Scala 2.13 and below](${
                                  dependencyExplorerUrl(
                                    group    = "org.scala-lang",
                                    artefact = "scala-library",
                                    flag     = "production",
                                    team     = teamName
                                  )
                                }) and [repos now using Scala 3](${
                                  dependencyExplorerUrl(
                                    group    = "org.scala-lang",
                                    artefact = "scala3-library",
                                    flag     = "production",
                                    team     = teamName
                                  )
                                }) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=774373449"}).""",
        fromArtefacts         = Seq(Artefact("org.scala-lang", "scala-library")),
        toArtefacts           = Seq(Artefact("org.scala-lang", "scala3-library")),
        team                  = teamName
      ),
      createMigrationInitiative(
        initiativeName        = "Play 3.0 upgrade - Production",
        initiativeDescription = s"""Play 3.0 upgrade - Deprecate [Play 2.9 and below](${
                                  dependencyExplorerUrl(
                                    group        = "com.typesafe.play",
                                    artefact     = "play",
                                    flag         = "production",
                                    team         = teamName,
                                    versionRange = Some("[0.0.0,3.0.0)")
                                  )
                                }) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=774373449"}).""",
        fromArtefacts         = Seq(Artefact("com.typesafe.play", "play")),
        toArtefacts           = Seq(Artefact("org.playframework", "play")),
        team                  = teamName
      ),
      createMigrationInitiative(
        initiativeName        = "Play 3.0 upgrade - Latest",
        initiativeDescription = s"""Play 3.0 upgrade - Deprecate [Play 2.9 and below](${
                                  dependencyExplorerUrl(
                                    group        = "com.typesafe.play",
                                    artefact     = "play",
                                    team         = teamName,
                                    repoTypes    = Seq("Service", "Library", "Test", "Other"),
                                    versionRange = Some("[0.0.0,3.0.0)")
                                  )
                                }) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=774373449"}).""",
        fromArtefacts         = Seq(Artefact("com.typesafe.play", "play")),
        toArtefacts           = Seq(Artefact("org.playframework", "play")),
        team                  = teamName,
        environment           = None
      ),
      createUpgradeInitiative(
        initiativeName        = "Scala 2.13 Upgrade",
        initiativeDescription = s"""Scala 2.13 upgrade - Deprecate [Scala 2.12 and below](${
                                  dependencyExplorerUrl(
                                    group        = "org.scala-lang",
                                    artefact     = "scala-library",
                                    flag         = "production",
                                    team         = teamName,
                                    versionRange = Some("[0.0.0,2.13.0)")
                                  )
                                }) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=275944511"}).""",
        group                 = "org.scala-lang",
        artefact              = "scala-library",
        version               = Version("2.13.0"),
        team                  = teamName,
      ),
      createMigrationInitiative(
        initiativeName        = "Replace simple-reactivemongo with hmrc-mongo",
        initiativeDescription = s"""Monitoring [repos still using simple-reactivemongo](${
                                  dependencyExplorerUrl(
                                    group    = "uk.gov.hmrc",
                                    artefact = "simple-reactivemongo",
                                    flag     = "production",
                                    team     = teamName
                                  )
                                }) and [repos now using hmrc-mongo](${
                                  dependencyExplorerUrl(
                                    group    = "uk.gov.hmrc.mongo",
                                    artefact = "hmrc-mongo-common",
                                    flag     = "production",
                                    team     = teamName
                                  )
                                }) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/display/TEC/2021/03/04/HMRC+Mongo+is+now+available"}).""",
        fromArtefacts         = Seq(Artefact("uk.gov.hmrc", "simple-reactivemongo")),
        toArtefacts           = Seq(Artefact("uk.gov.hmrc.mongo", "hmrc-mongo-common")),
        team                  = teamName,
        inProgressLegend      = "Simple-Reactivemongo",
        completedLegend       = "HMRC-Mongo"
      ),
      createJavaInitiative(
        initiativeName        = "Java 11 Upgrade",
        initiativeDescription = s"""[Java 11 upgrade](${
                                  url"https://catalogue.tax.service.gov.uk/jdkexplorer/latest?teamName=$teamName"
                                }) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/x/TwLIH"})""",
        version               = Version("11.0.0"),
        team                  = teamName
      ),
      createJavaInitiative(
        initiativeName        = "Java 21 Upgrade",
        initiativeDescription = s"""[Java 21 upgrade](${
                                  url"https://catalogue.tax.service.gov.uk/jdkexplorer/latest?teamName=$teamName"
                                }) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/x/xIACM"})""",
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

  def dependencyExplorerUrl(
    group       : String,
    artefact    : String,
    flag        : String         = "latest",
    versionRange: Option[String] = None,
    team        : Option[String] = None,
    repoTypes   : Seq[String]    = Seq.empty, // Note, default is Service
    scopes      : Seq[String]    = Seq.empty, // Note, default is Compile
  ): String = {
    url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=$group&artefact=$artefact&versionRange=$versionRange&team=$team&flag=$flag&repoType[]=$repoTypes&scope[]=$scopes"
      .toString
      .replace(")", "\\)") // for markdown
  }
}
