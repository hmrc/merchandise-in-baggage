import sbt.*

object AppDependencies {

  private lazy val bootstrapPlayVersion = "9.0.0"
  private lazy val hmrcMongoVersion     = "2.1.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                 % "2.12.0",
    // need to match playframework version from plugin and bootstrap and this library in this case now it is 3.0.3 everywhere
    "com.beachape"      %% "enumeratum-play"           % "1.8.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapPlayVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID]      = compile ++ test

}
