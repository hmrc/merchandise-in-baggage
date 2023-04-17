import play.sbt.PlayImport.PlayKeys.playDefaultPort
import scoverage.ScoverageKeys

val appName = "merchandise-in-baggage"

val silencerVersion = "1.7.0"

val contractVerifier = taskKey[Unit]("Launch contract tests")

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, ScalaPactPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.12.11",
    playDefaultPort                  := 8280,
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    // ***************
    // Use the silencer plugin to suppress warnings
    scalacOptions += "-P:silencer:pathFilters=routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
    // ***************
  )
  .settings(inConfig(Test)(testSettings))
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(
    retrieveManaged := true,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true
  ).settings(
  routesImport ++= Seq("uk.gov.hmrc.merchandiseinbaggage.binders.PathBinders._", "uk.gov.hmrc.merchandiseinbaggage.model.core._", "uk.gov.hmrc.merchandiseinbaggage.model.api._"),
)
  .settings(Test / testOptions := Seq(Tests.Filter {
    name =>
      val pactDir = new File("../merchandise-in-baggage-frontend/pact")
      val noContracts = !pactDir.exists() || pactDir.listFiles().isEmpty
      if(noContracts) !name.endsWith("VerifyContractSpec") else name.endsWith("Spec")
  }))
  .settings(
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*BuildInfo.*;.*javascript.*;.*Routes.*;.*testonly.*;.*mongojob.*;.*binders.*;.*.config.*;.*PagerDutyHelper.*",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
  )

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork        := true,
  javaOptions ++= Seq(
    "-Dlogger.resource=logback-test.xml",
    "-Dconfig.resource=test.application.conf"
  )
)

contractVerifier := (Test / testOnly).toTask(" *VerifyContractSpec").value
