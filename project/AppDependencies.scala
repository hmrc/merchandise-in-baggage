import play.core.PlayVersion.current
import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  private val pactVersion          = "4.4.0"
  private val hmrcMongoVersion     = "0.68.0"
  private val bootstrapPlayVersion = "7.15.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc"                %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-28"        % hmrcMongoVersion,
    "com.github.pureconfig"      %% "pureconfig"                % "0.17.3",
    "org.typelevel"              %% "cats-core"                 % "2.9.0",
    "com.beachape"               %% "enumeratum-play"           % "1.7.0",
    "com.softwaremill.quicklens" %% "quicklens"                 % "1.9.2"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapPlayVersion,
    "org.scalatest"          %% "scalatest"               % "3.2.15",
    "com.typesafe.play"      %% "play-test"               % current,
    "com.vladsch.flexmark"    % "flexmark-all"            % "0.64.0",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0",
    "org.scalamock"          %% "scalamock"               % "5.2.0",
    "com.github.tomakehurst"  % "wiremock-standalone"     % "2.27.2",
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % hmrcMongoVersion,
    "com.itv"                %% "scalapact-circe-0-13"    % pactVersion,
    "com.itv"                %% "scalapact-http4s-0-21"   % pactVersion,
    "com.itv"                %% "scalapact-scalatest"     % pactVersion,
    "org.scalaj"             %% "scalaj-http"             % "2.4.2",
    "org.json4s"             %% "json4s-native"           % "3.6.12"
  ).map(_ % "test")
}
