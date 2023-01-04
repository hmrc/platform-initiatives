import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin

lazy val microservice = Project("platform-initiatives", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(majorVersion := 0)
  .settings(SbtDistributablesPlugin.publishingSettings: _*)
  .settings(PlayKeys.playDefaultPort := 9021)
  .settings(libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(scalaVersion := "2.13.10")
  .settings(scalacOptions += "-Wconf:src=routes/.*:s")
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
