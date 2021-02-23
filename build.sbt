import play.sbt.PlayImport.PlayKeys.playDefaultPort
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "merchandise-in-baggage"

val silencerVersion = "1.7.0"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory, ScalaPactPlugin)
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
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(
    retrieveManaged := true,
    scalafmtOnCompile in Compile := true,
    scalafmtOnCompile in Test := true
  ).settings(
  routesImport ++= Seq("uk.gov.hmrc.merchandiseinbaggage.binders.PathBinders._", "uk.gov.hmrc.merchandiseinbaggage.model.core._", "uk.gov.hmrc.merchandiseinbaggage.model.api._"),
)
  .settings(Test / testOptions := Seq(Tests.Filter {
    name =>
      val noContracts = new File("../merchandise-in-baggage-frontend/pact").listFiles().isEmpty
      if(noContracts) !name.endsWith("VerifyContractSpec") else name.endsWith("Spec")
  }
  )) //TODO make it work on pipeline.
