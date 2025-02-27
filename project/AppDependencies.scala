import sbt._

object AppDependencies {
  val bootstrapPlayVersion = "9.10.0"

  val compile = Seq(
    "uk.gov.hmrc"    %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "org.typelevel"  %% "cats-core"                 % "2.13.0"
  )

  val test = Seq(
    "uk.gov.hmrc"    %% "bootstrap-test-play-30"    % bootstrapPlayVersion  % Test
  )
}
