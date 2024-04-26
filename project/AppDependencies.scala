import sbt.*

object AppDependencies {

  private val pactVersion = "4.4.0"

  private lazy val bootstrapPlayVersion = "8.5.0"
  private lazy val hmrcMongoVersion     = "1.8.0"

  private val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"           %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-play-30"        % hmrcMongoVersion,
    "com.github.pureconfig" %% "pureconfig"                % "0.17.6",
    "org.typelevel"         %% "cats-core"                 % "2.10.0",
    "com.beachape"          %% "enumeratum-play"           % "1.8.0"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "uk.gov.hmrc"                %% "bootstrap-test-play-30"  % bootstrapPlayVersion,
    "org.scalamock"              %% "scalamock"               % "6.0.0",
    "com.itv"                    %% "scalapact-circe-0-13"    % pactVersion,
    "com.itv"                    %% "scalapact-http4s-0-21"   % pactVersion,
    "com.itv"                    %% "scalapact-scalatest"     % pactVersion,
    "com.softwaremill.quicklens" %% "quicklens"               % "1.9.7"
  ).map(_ % "test")

  def apply(): Seq[ModuleID]      = compile ++ test
}
