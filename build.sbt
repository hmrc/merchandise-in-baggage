import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption

val appName = "merchandise-in-baggage"

ThisBuild / scalaVersion := "2.13.13"
ThisBuild / majorVersion := 0

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(CodeCoverageSettings.settings)
  .settings(
    PlayKeys.playDefaultPort := 8280,
    libraryDependencies ++= AppDependencies(),
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-Wconf:cat=unused-imports&src=views/.*:s"
    )
  )
  .settings(
    routesImport ++= Seq(
      "uk.gov.hmrc.merchandiseinbaggage.binders.PathBinders._",
      "uk.gov.hmrc.merchandiseinbaggage.model.core._",
      "uk.gov.hmrc.merchandiseinbaggage.model.api._"
    )
  )
  .settings(addTestReportOption(Test, "test-reports"))

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle")
