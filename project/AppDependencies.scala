import sbt.*

object AppDependencies {

  private lazy val bootstrapPlayVersion = "8.6.0"
  private lazy val hmrcMongoVersion     = "1.9.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                 % "2.12.0",
    "com.beachape"      %% "enumeratum-play"           % "1.8.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapPlayVersion,
    "org.scalamock"     %% "scalamock"               % "6.0.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID]      = compile ++ test
}
