import sbt.*

object AppDependencies {

  private lazy val bootstrapPlayVersion = "9.12.0"
  private lazy val hmrcMongoVersion     = "2.6.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                 % "2.13.0",
    "com.beachape"      %% "enumeratum-play"           % "1.8.2"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapPlayVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
