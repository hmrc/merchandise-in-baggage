import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val pactVersion = "4.4.0"
  private val hmrcMongoVersion = "0.68.0"

  val compile = Seq(
    "uk.gov.hmrc"                %% "bootstrap-backend-play-28"  % "5.24.0",
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "com.github.pureconfig"      %% "pureconfig"                 % "0.17.1",
    "org.typelevel"              %% "cats-core"                  % "2.7.0",
    "com.beachape"               %% "enumeratum-play"            % "1.7.0",
    "com.softwaremill.quicklens" %% "quicklens"                  % "1.8.4"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % "5.24.0"          % Test,
    "org.scalatest"           %% "scalatest"                % "3.2.11"          % Test,
    "com.typesafe.play"       %% "play-test"                % current           % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.35.10"         % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"           % Test,
    "org.scalamock"           %% "scalamock"                % "5.2.0"           % Test,
    "com.github.tomakehurst"  %  "wiremock-standalone"      % "2.27.2"          % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"  % hmrcMongoVersion  % Test,
    "com.itv"                 %% "scalapact-circe-0-13"     % pactVersion       % Test,
    "com.itv"                 %% "scalapact-http4s-0-21"    % pactVersion       % Test,
    "com.itv"                 %% "scalapact-scalatest"      % pactVersion       % Test,
    "org.scalaj"              %% "scalaj-http"              % "2.4.2"           % Test,
    "org.json4s"              %% "json4s-native"            % "3.6.12"          % Test
  )
}
