ThisBuild / scalaVersion := "2.13.14"
ThisBuild / majorVersion := 0

lazy val microservice = Project("merchandise-in-baggage", file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(CodeCoverageSettings.settings)
  .settings(
    PlayKeys.playDefaultPort := 8280,
    libraryDependencies ++= AppDependencies(),
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s"
    )
  )
  .settings(
    routesImport ++= Seq(
      "uk.gov.hmrc.merchandiseinbaggage.binders.PathBinders._",
      "uk.gov.hmrc.merchandiseinbaggage.model.core._",
      "uk.gov.hmrc.merchandiseinbaggage.model.api._"
    )
  )

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")
