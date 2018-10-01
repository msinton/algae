import ReleaseTransformations._

lazy val algae = project
  .in(file("."))
  .settings(
    moduleName := "algae",
    name := moduleName.value
  )
  .settings(commonSettings, noPublishSettings)
  .settings(
    console := (console in (core, Compile)).value,
    console in Test := (console in (core, Test)).value
  )
  .aggregate(
    core,
    laws,
    ciris,
    `ciris-kubernetes`,
    kamon,
    `kamon-influxdb`,
    `kamon-system-metrics`,
    slf4j,
    logback,
    tests
  )

lazy val core = project
  .in(file("core"))
  .settings(
    moduleName := "algae-core",
    name := moduleName.value
  )
  .settings(commonSettings)
  .settings(cats, catsMtl, catsEffect, kindProjector)

lazy val docs = project
  .in(file("docs"))
  .settings(
    moduleName := "algae-docs",
    name := moduleName.value
  )
  .settings(commonSettings, noPublishSettings)
  .settings(
    tutTargetDirectory := (baseDirectory in algae).value,
    scalacOptions --= Seq("-Xlint", "-Ywarn-unused"),
    buildInfoObject := "info",
    buildInfoPackage := "algae.build",
    buildInfoKeys := Seq[BuildInfoKey](
      latestVersion in ThisBuild
    )
  )
  .enablePlugins(BuildInfoPlugin, TutPlugin)
  .dependsOn(core, slf4j)

lazy val laws = project
  .in(file("laws"))
  .settings(
    moduleName := "algae-laws",
    name := moduleName.value
  )
  .settings(commonSettings)
  .settings(catsLaws, catsMtlLaws, catsEffectLaws)
  .dependsOn(core)

lazy val ciris = project
  .in(file("ciris"))
  .settings(
    moduleName := "algae-ciris",
    name := moduleName.value
  )
  .settings(commonSettings)
  .settings(cirisCore, cirisCatsEffect, kindProjector)
  .dependsOn(core)

lazy val `ciris-kubernetes` = project
  .in(file("ciris-kubernetes"))
  .settings(
    moduleName := "algae-ciris-kubernetes",
    name := moduleName.value
  )
  .settings(commonSettings)
  .settings(cirisKubernetes, kindProjector)
  .dependsOn(ciris)

lazy val kamon = project
  .in(file("kamon"))
  .settings(
    moduleName := "algae-kamon",
    name := moduleName.value
  )
  .settings(commonSettings)
  .settings(kamonCore)
  .dependsOn(core)

lazy val `kamon-influxdb` = project
  .in(file("kamon-influxdb"))
  .settings(
    moduleName := "algae-kamon-influxdb",
    name := moduleName.value
  )
  .settings(commonSettings)
  .settings(kamonInfluxDb)
  .dependsOn(kamon)

lazy val `kamon-system-metrics` = project
  .in(file("kamon-system-metrics"))
  .settings(
    moduleName := "algae-kamon-system-metrics",
    name := moduleName.value
  )
  .settings(commonSettings)
  .settings(kamonSystemMetrics)
  .dependsOn(kamon)

lazy val slf4j = project
  .in(file("slf4j"))
  .settings(
    moduleName := "algae-slf4j",
    name := moduleName.value
  )
  .settings(commonSettings)
  .settings(slf4jApi)
  .dependsOn(core)

lazy val logback = project
  .in(file("logback"))
  .settings(
    moduleName := "algae-logback",
    name := moduleName.value
  )
  .settings(commonSettings)
  .settings(logbackClassic)
  .dependsOn(slf4j)

lazy val tests = project
  .in(file("tests"))
  .settings(commonSettings, noPublishSettings)
  .settings(scalaTest)
  .dependsOn(core, laws)

lazy val commonSettings =
  scalaSettings ++
    testSettings ++
    publishSettings ++
    resolverSettings

lazy val scalaSettings = Seq(
  scalaVersion := "2.12.7",
  crossScalaVersions := Seq(scalaVersion.value, "2.11.12"),
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture",
    "-Ywarn-unused"
  ),
  scalacOptions in (Compile, console) --= Seq("-Xlint", "-Ywarn-unused"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
)

lazy val testSettings = Seq(
  logBuffered in Test := false,
  parallelExecution in Test := false
)

lazy val publishSettings = Seq(
  organization := "com.ovoenergy",
  bintrayOrganization := Some("ovotech"),
  bintrayRepository := "maven",
  licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
  publishArtifact in Test := false,
  releaseCrossBuild := true,
  releaseTagName := s"v${(version in ThisBuild).value}",
  releaseTagComment := s"Release version ${(version in ThisBuild).value}",
  releaseCommitMessage := s"Set version to ${(version in ThisBuild).value}",
  releaseUseGlobalVersion := true,
  publishMavenStyle := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    setReleaseVersion,
    setLatestVersion,
    releaseStepTask(updateReadme in ThisBuild),
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

lazy val resolverSettings = Seq(
  resolvers += Resolver.bintrayRepo("ovotech", "maven")
)

lazy val noPublishSettings = Seq(
  skip in publish := true,
  publishArtifact := false
)

val updateReadme = TaskKey[Unit]("updateReadme", "Update the readme with tut and vcs")
updateReadme in ThisBuild := {
  val _ = (tut in docs).value
  sbtrelease.Vcs.detect((baseDirectory in algae).value).foreach { vcs ⇒
    vcs.add("readme.md").!
    vcs.commit("Update readme to latest version [ci skip]", sign = false).!
  }
}
