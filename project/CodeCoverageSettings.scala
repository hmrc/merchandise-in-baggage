import sbt.Setting
import scoverage.ScoverageKeys.*

object CodeCoverageSettings {

  private val excludedPackages: Seq[String] = Seq(
    "<empty>",
    "Reverse.*",
    ".*BuildInfo.*",
    ".*javascript.*",
    ".*Routes.*",
    ".*testonly.*",
    ".*mongojob.*",
    ".*binders.*",
    ".*.config.*",
    ".*PagerDutyHelper.*"
  )

  val settings: Seq[Setting[?]] =
    Seq(
      coverageExcludedPackages := excludedPackages.mkString(";"),
      coverageMinimumStmtTotal := 91,
      coverageFailOnMinimum := true,
      coverageHighlighting := true
    )

}
