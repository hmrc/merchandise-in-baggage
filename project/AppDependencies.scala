import sbt.*

object AppDependencies {

  private val pactVersion          = "4.4.0"
  private val hmrcMongoVersion     = "1.3.0"
  private val bootstrapPlayVersion = "7.21.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-28"        % hmrcMongoVersion,
    "com.github.pureconfig"      %% "pureconfig"                % "0.17.4",
    "org.typelevel"              %% "cats-core"                 % "2.10.0",
    "com.beachape"               %% "enumeratum-play"           % "1.7.0", //versions 1.7.1+ are not binary compatible with previous ones
    "com.softwaremill.quicklens" %% "quicklens"                 % "1.9.6"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % hmrcMongoVersion,
    "org.scalatest"          %% "scalatest"               % "3.2.16",
    "com.vladsch.flexmark"    % "flexmark-all"            % "0.64.8",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0",
    "org.scalamock"          %% "scalamock"               % "5.2.0",
    "com.github.tomakehurst"  % "wiremock-standalone"     % "2.27.2",
    "com.itv"                %% "scalapact-circe-0-13"    % pactVersion,
    "com.itv"                %% "scalapact-http4s-0-21"   % pactVersion,
    "com.itv"                %% "scalapact-scalatest"     % pactVersion
  ).map(_ % "test")
}
