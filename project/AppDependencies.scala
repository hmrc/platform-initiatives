import play.sbt.PlayImport.{ehcache, ws}

import sbt._


object AppDependencies {
  val bootstrapPlayVersion = "9.0.0"

  val compile = Seq(
    ws,
    ehcache,
    "uk.gov.hmrc"    %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "org.typelevel"  %% "cats-core"                 % "2.10.0"
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % bootstrapPlayVersion  % Test,
    "org.scalatestplus" %% "mockito-3-4"            % "3.2.10.0"            % Test
  )
}
