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
    // To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    PlayKeys.playDefaultPort := 8280,
    libraryDependencies ++= AppDependencies(),
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-Wconf:cat=unused-imports&src=views/.*:s",
      "-Xlint:-byname-implicit" //to silence: Block result was adapted via implicit conversion warnings: https://github.com/scala/bug/issues/12072
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

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    Test / fork := true
  )

val contractVerifier = taskKey[Unit]("Launch contract tests")

contractVerifier := (Test / testOnly).toTask(" *VerifyContractSpec").value

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle it/Test/scalastyle")
