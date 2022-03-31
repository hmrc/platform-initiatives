import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val silencerVersion = "1.7.5"

lazy val microservice = Project("platform-initiatives", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(majorVersion := 0)
  .settings(SbtDistributablesPlugin.publishingSettings: _*)
  .settings(PlayKeys.playDefaultPort := 9021)
  .settings(libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scalaVersion := "2.12.14")
  .settings(
    // Use the silencer plugin to suppress warnings from unused imports in routes etc.
    scalacOptions += "-P:silencer:pathFilters=routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
