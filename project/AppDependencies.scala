import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val pactVersion = "3.2.0"
  private val hmrcMongoVersion = "0.59.0"

  val compile = Seq(
    "uk.gov.hmrc"                %% "bootstrap-backend-play-28"  % "5.3.0",
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "com.github.pureconfig"       %% "pureconfig"                  % "0.13.0",
    "org.typelevel"              %% "cats-core"                  % "2.2.0",
    "com.beachape"               %% "enumeratum-play"            % "1.5.13",
    "uk.gov.hmrc"                %% "crypto"                     % "6.0.0",
    "com.softwaremill.quicklens" %% "quicklens"                  % "1.6.1"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"   % "5.3.0"  % Test,
    "org.scalatest"           %% "scalatest"                % "3.2.9"   % Test,
    "com.typesafe.play"       %% "play-test"                % current   % Test,
    "com.vladsch.flexmark"     %  "flexmark-all"              % "0.35.10" % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "5.1.0"   % "test, it",
    "org.scalamock"           %% "scalamock"                % "4.4.0"   % Test,
    "com.github.tomakehurst"  %  "wiremock-standalone"      % "2.27.1"  % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"  % hmrcMongoVersion  % Test,
    "com.itv"                 %% "scalapact-circe-0-13"     % pactVersion    % Test,
    "com.itv"                 %% "scalapact-http4s-0-21"    % pactVersion    % Test,
    "com.itv"                 %% "scalapact-scalatest"      % pactVersion    % Test,
    "org.scalaj"              %% "scalaj-http"              % "2.4.2"    % Test,
    "org.json4s"              %% "json4s-native"            % "3.6.9"    % Test
  )
}
