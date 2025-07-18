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

package uk.gov.hmrc.platforminitiatives.service

import cats.implicits.*
import play.api.Configuration
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.platforminitiatives.connector.{ServiceConfigsConnector, ServiceDependenciesConnector}
import uk.gov.hmrc.platforminitiatives.model.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PlatformInitiativesService @Inject()(
  configuration                 : Configuration,
  serviceConfigsConnector       : ServiceConfigsConnector,
  serviceDependenciesConnector  : ServiceDependenciesConnector,
  cc                            : ControllerComponents
) extends BackendController(cc):

  private given HeaderCarrier = HeaderCarrier()

  private val displayExperimentalInitiatives: Boolean =
    configuration.get[Boolean]("initiatives.service.includeExperimental")

  def platformInitiatives(
    teamName      : Option[String],
    digitalService: Option[String]
  )(using ExecutionContext): Future[Seq[PlatformInitiative]] =
    Seq(
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
        digitalService        = digitalService,
        environment           = None,
        scopes                = Seq(DependencyScope.Test)
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
        team                  = teamName,
        digitalService        = digitalService
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
        team                  = teamName,
        digitalService        = digitalService
      ),
      createMigrationInitiative(
        initiativeName        = "Play 3.0 Upgrade - Production",
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
        team                  = teamName,
        digitalService        = digitalService
      ),
      createMigrationInitiative(
        initiativeName        = "Play 3.0 Upgrade - Latest",
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
        digitalService        = digitalService,
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
        digitalService        = digitalService
      ),
      createJavaInitiative(
        initiativeName        = "Java 11 Upgrade",
        initiativeDescription = s"""[Java 11 upgrade](${
                                  url"https://catalogue.tax.service.gov.uk/jdkexplorer/environment?env=latest&teamName=$teamName"
                                }) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/x/TwLIH"})""",
        version               = Version("11.0.0"),
        team                  = teamName,
        digitalService        = digitalService
      ),
      createJavaInitiative(
        initiativeName        = "Java 21 Upgrade",
        initiativeDescription = s"""[Java 21 upgrade](${
                                  url"https://catalogue.tax.service.gov.uk/jdkexplorer/environment?env=latest&teamName=$teamName"
                                }) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/x/xIACM"})""",
        version               = Version.apply("21.0.0"),
        team                  = teamName,
        digitalService        = digitalService
      ),
      createGovUkBrandInitiative(
        team                  = teamName,
        digitalService        = digitalService
      )
    ).sequence
     .map(
       _.filter(_.progress.target != 0)
        .filter(!_.experimental || displayExperimentalInitiatives)
     )

  def createGovUkBrandInitiative(
    team          : Option[String],
    digitalService: Option[String]
  )(implicit
    ec: ExecutionContext
  ): Future[PlatformInitiative] =
    ( Seq("play-frontend-hmrc-play-28", "play-frontend-hmrc-play-29", "play-frontend-hmrc-play-30")
      .foldLeftM(Seq.empty[MetaArtefactDependency]): (acc, artefact) =>
        serviceDependenciesConnector
          .getMetaArtefactDependency("uk.gov.hmrc", artefact, Some(Environment.Production), Seq(DependencyScope.Compile))
            .map(_ ++ acc)
      .map(_.filter(dependency => team.fold(true)(dependency.teams == Seq(_)))) // Filtering for exclusively owned repos
      .map(_.filter(dependencies => digitalService.fold(true)(x => dependencies.digitalService.exists(_ == x))))
    , serviceConfigsConnector
        .searchConfig(
          key            = "play-frontend-hmrc.useRebrand",
          value          = "true",
          environment    = Seq(Environment.Production),
          team           = team,
          digitalService = digitalService
        )
    ).mapN: (dependencies, withConfig) =>
      val repoLinks =
        Seq(
          "play-frontend-hmrc-play-28" -> "28",
          "play-frontend-hmrc-play-29" -> "29",
          "play-frontend-hmrc-play-30" -> "30"
        )
          .map((artefact, label) => s"[$label](${
            dependencyExplorerUrl(
              group    = "uk.gov.hmrc",
              artefact = artefact,
              flag     = "production",
              team     = team
            )})")
      val upgradedLinks =
        Seq(
          "play-frontend-hmrc-play-29" -> "29",
          "play-frontend-hmrc-play-30" -> "30"
        )
          .map((artefact, label) => s"[$label](${
            dependencyExplorerUrl(
              group        = "uk.gov.hmrc",
              artefact     = artefact,
              flag         = "production",
              versionRange = Some("[12.3.0,]"),
              team         = team
            )})")
      PlatformInitiative(
        initiativeName          = "GOV•UK Brand Refresh",
        initiativeDescription   = s"""Repos using `play-frontend-hmrc` ${repoLinks.mkString("(", " | ", ")")} require version 12.3.0+ ${upgradedLinks.mkString("(", " | ", ")")} and enabling with [config](${
                                    searchConfigUrl(
                                      key         = "play-frontend-hmrc.useRebrand",
                                      value       = "true",
                                      team        = team,
                                      environment = "production"
                                    )
                                  }) | [Confluence](${url"https://confluence.tools.tax.service.gov.uk/x/jYGZQ"})""",
        progress                = Progress(
                                    current = dependencies.filter(d => Version(d.depVersion) >= Version("12.3.0"))
                                                .map(_.repoName).intersect(withConfig.map(_.serviceName))
                                                .size,
                                    target  = dependencies.length
                                  ),
        completedLegend         = "Completed",
        inProgressLegend        = "Not Completed",
        experimental            = false
      )


  def createJavaInitiative(
    initiativeName       : String,
    initiativeDescription: String,
    version              : Version,
    team                 : Option[String],
    digitalService       : Option[String],
    completedLegend      : String         = "Completed",
    inProgressLegend     : String         = "Not Completed",
    experimental         : Boolean        = false
  )(implicit
    ec: ExecutionContext
  ): Future[PlatformInitiative] =
    serviceDependenciesConnector
      .getSlugJdkVersions(team, digitalService)
      .map: repos =>
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

  def createUpgradeInitiative(
    initiativeName       : String,
    initiativeDescription: String,
    group                : String,
    artefact             : String,
    version              : Version,
    team                 : Option[String]       = None,
    digitalService       : Option[String]       = None,
    environment          : Option[Environment]  = Some(Environment.Production),
    scopes               : Seq[DependencyScope] = Seq(DependencyScope.Compile),
    completedLegend      : String               = "Completed",
    inProgressLegend     : String               = "Not Completed",
    experimental         : Boolean              = false
  )(implicit
    ec                   : ExecutionContext
  ): Future[PlatformInitiative] =
    serviceDependenciesConnector
      .getMetaArtefactDependency(group, artefact, environment, scopes)
      .map(_.filter(dependency => team.fold(true)(dependency.teams == Seq(_))))  // Filtering for exclusively owned repos, if set
      .map(_.filter(dependency => digitalService.fold(true)(x => dependency.digitalService.exists(_ == x))))
      .map: dependencies =>
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

  def createMigrationInitiative(
    initiativeName       : String,
    initiativeDescription: String,
    fromArtefacts        : Seq[Artefact],
    toArtefacts          : Seq[Artefact],
    targetVersion        : Option[Version]      = None,
    team                 : Option[String]       = None,
    digitalService       : Option[String]       = None,
    environment          : Option[Environment]  = Some(Environment.Production),
    scopes               : Seq[DependencyScope] = Seq(DependencyScope.Compile),
    completedLegend      : String               = "Completed",
    inProgressLegend     : String               = "Not Completed",
    experimental         : Boolean              = false
  )(implicit
    ec                   : ExecutionContext
  ): Future[PlatformInitiative] =
    for
      fromDependencies     <- fromArtefacts
                                .traverse(a => serviceDependenciesConnector.getMetaArtefactDependency(a.group, a.name, environment, scopes))
                                .map(_.flatten)
                                .map(_.filter(dependency => team.fold(true)(dependency.teams == Seq(_)))) // Filtering for exclusively owned repos
                                .map(_.filter(dependency => digitalService.fold(true)(x => dependency.digitalService.exists(_ == x))))
      targetDependencies   <- toArtefacts
                                .traverse(a => serviceDependenciesConnector.getMetaArtefactDependency(a.group, a.name, environment, scopes))
                                .map(_.flatten)
                                .map(_.filter(dependency => team.fold(true)(dependency.teams == Seq(_)))) // Filtering for exclusively owned repos
                                .map(_.filter(dependency => digitalService.fold(true)(x => dependency.digitalService.exists(_ == x))))
      allDependencies       = fromDependencies ++ targetDependencies
    yield PlatformInitiative(
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
  ): String =
    url"https://catalogue.tax.service.gov.uk/dependencyexplorer/results?group=$group&artefact=$artefact&versionRange=$versionRange&team=$team&flag=$flag&repoType[]=$repoTypes&scope[]=$scopes"
      .toString
      .replace(")", "\\)") // for markdown

  def searchConfigUrl(
    key           : String,
    value         : String,
    team          : Option[String] = None,
    environment   : String
  ): String =
    url"https://catalogue.tax.service.gov.uk/config/search/results?teamName=$team&configKey=$key&showEnvironments[]=$environment&configValue=$value&valueFilterType=equalTo"
      .toString
