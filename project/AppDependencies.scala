import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val pactVersion = "3.2.0"

  val compile = Seq(
    "uk.gov.hmrc"                %% "bootstrap-backend-play-27"  % "3.0.0",
    "uk.gov.hmrc"                %% "simple-reactivemongo"       % "7.30.0-play-27",
    "com.github.pureconfig"      %% "pureconfig"                 % "0.13.0",
    "org.typelevel"              %% "cats-core"                  % "2.0.0",
    "com.beachape"               %% "enumeratum-play"            % "1.5.13",
    "com.softwaremill.quicklens" %% "quicklens"                  % "1.6.1"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-27"   % "2.24.0"  % Test,
    "org.scalatest"           %% "scalatest"                % "3.1.2"   % Test,
    "com.typesafe.play"       %% "play-test"                % current   % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"             % "0.35.10" % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "4.0.3"   % "test, it",
    "de.bwaldvogel"           %  "mongo-java-server"        % "1.34.0"  % Test,
    "org.scalamock"           %% "scalamock"                % "4.4.0"   % Test,
    "com.github.tomakehurst"  %  "wiremock-standalone"      % "2.27.1"  % Test,

    "com.itv"                  %% "scalapact-circe-0-13"    % pactVersion    % Test,
    "com.itv"                  %% "scalapact-http4s-0-21"   % pactVersion    % Test,
    "com.itv"                  %% "scalapact-scalatest"     % pactVersion    % Test,
    "org.scalaj"               %% "scalaj-http"             % "2.4.2"    % Test,
    "org.json4s"               %% "json4s-native"           % "3.6.9"    % Test
  )
}
