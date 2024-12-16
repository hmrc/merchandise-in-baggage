ThisBuild / scalaVersion := "3.5.1"
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
      "-Wconf:msg=unused import&src=conf/.*:s",
      "-Wconf:src=routes/.*:s"
    )
  )
  .settings(
    routesImport ++= Seq(
      "uk.gov.hmrc.merchandiseinbaggage.binders.PathBinders.*",
      "uk.gov.hmrc.merchandiseinbaggage.model.core.*",
      "uk.gov.hmrc.merchandiseinbaggage.model.api.*"
    )
  )

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")
