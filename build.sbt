import play.sbt.PlayImport.PlayKeys.playDefaultPort
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import scoverage.ScoverageKeys

val appName = "merchandise-in-baggage"

val contractVerifier = taskKey[Unit]("Launch contract tests")

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, ScalaPactPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion := 0,
    scalaVersion := "2.13.10",
    // To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    playDefaultPort := 8280,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-Wconf:cat=unused-imports&src=views/.*:s"
    )
  )
  .settings(inConfig(Test)(testSettings))
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(
    retrieveManaged := true,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true
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
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*BuildInfo.*;.*javascript.*;.*Routes.*;.*testonly.*;.*mongojob.*;.*binders.*;.*.config.*;.*PagerDutyHelper.*",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork := true,
  javaOptions ++= Seq(
    "-Dlogger.resource=logback-test.xml",
    "-Dconfig.resource=test.application.conf"
  )
)

contractVerifier := (Test / testOnly).toTask(" *VerifyContractSpec").value

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle")
