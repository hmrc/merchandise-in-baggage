import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption

val appName = "merchandise-in-baggage"

ThisBuild / scalaVersion := "2.13.13"
ThisBuild / majorVersion := 0


lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, ScalaPactPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(CodeCoverageSettings.settings)
  .settings(
    // this scala-xml version scheme is to get around some library dependency conflicts
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
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
  .settings(Test / testOptions := Seq(Tests.Filter { name =>
    val pactDir     = new File("../merchandise-in-baggage-frontend/pact")
    val noContracts = !pactDir.exists() || pactDir.listFiles().isEmpty
    if (noContracts) !name.endsWith("VerifyContractSpec") else name.endsWith("Spec")
  }))
  .settings(addTestReportOption(Test, "test-reports"))

val contractVerifier = taskKey[Unit]("Launch contract tests")

contractVerifier := (Test / testOnly).toTask(" *VerifyContractSpec").value

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle it/Test/scalastyle")
