import sbt.*

object AppDependencies {

  private val pactVersion          = "4.4.0"
  private val hmrcMongoVersion     = "1.3.0"
  private val bootstrapPlayVersion = "7.22.0"

  private val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-28"        % hmrcMongoVersion,
    "com.github.pureconfig"      %% "pureconfig"                % "0.17.4",
    "org.typelevel"              %% "cats-core"                 % "2.10.0",
    "com.beachape"               %% "enumeratum-play"           % "1.7.0", //versions 1.7.1+ are not binary compatible with previous ones
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % hmrcMongoVersion,
    "org.scalatest"          %% "scalatest"               % "3.2.17",
    "com.vladsch.flexmark"    % "flexmark-all"            % "0.64.8",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0",
    "org.mockito"         %% "mockito-scala-scalatest" % "1.17.27",
    "org.scalamock"          %% "scalamock"               % "5.2.0",
    "org.wiremock"            % "wiremock-standalone"     % "3.2.0",
    "com.itv"                %% "scalapact-circe-0-13"    % pactVersion,
    "com.itv"                %% "scalapact-http4s-0-21"   % pactVersion,
    "com.itv"                %% "scalapact-scalatest"     % pactVersion,
    "com.softwaremill.quicklens" %% "quicklens"                 % "1.9.6"
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
