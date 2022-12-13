import play.sbt.PlayImport.{ehcache, ws}

import sbt._


object AppDependencies {
  val bootstrapPlayVersion = "7.0.0"
  val hmrcMongoVersion     = "0.70.0"

  val compile = Seq(
    ws,
    ehcache,
    "uk.gov.hmrc"           %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-metrix-play-28" % hmrcMongoVersion,
    "org.yaml"              %  "snakeyaml"                 % "1.28",
    "org.typelevel"         %% "cats-core"                 % "2.6.1"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"      % bootstrapPlayVersion  % "test, it",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"     % hmrcMongoVersion      % Test,
    "org.mockito"            %% "mockito-scala-scalatest"     % "1.16.23"             % Test
  )
}
