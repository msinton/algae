import sbt.Keys._
import sbt._

object Dependencies extends AutoPlugin {
  object Versions {
    val cats = "1.5.0"
    val catsMtl = "0.4.0"
    val catsEffect = "1.1.0"
    val ciris = "0.12.0"
    val cirisKubernetes = "0.10"
    val fs2Kafka = "0.17.3"
    val kamon = "1.1.3"
    val kamonInfluxDb = "1.0.2"
    val kamonSystemMetrics = "1.0.0"
    val kindProjector = "0.9.9"
    val logback = "1.2.3"
    val scalaTest = "3.0.5"
    val slf4j = "1.7.25"
  }

  object autoImport {
    val cats: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-core" % Versions.cats
      )
    )

    val catsLaws: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-laws" % Versions.cats
      )
    )

    val catsMtl: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-mtl-core" % Versions.catsMtl
      )
    )

    val catsMtlLaws: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-mtl-laws" % Versions.catsMtl
      )
    )

    val catsEffect: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-effect" % Versions.catsEffect
      )
    )

    val catsEffectLaws: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-effect-laws" % Versions.catsEffect
      )
    )

    val cirisCore: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "is.cir" %% "ciris-core" % Versions.ciris
      )
    )

    val cirisCatsEffect: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "is.cir" %% "ciris-cats-effect" % Versions.ciris
      )
    )

    val cirisKubernetes: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "com.ovoenergy" %% "ciris-kubernetes" % Versions.cirisKubernetes
      )
    )

    val fs2Kafka: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "com.ovoenergy" %% "fs2-kafka" % Versions.fs2Kafka
      )
    )

    val kamonCore: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "io.kamon" %% "kamon-core" % Versions.kamon
      )
    )

    val kamonInfluxDb: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "io.kamon" %% "kamon-influxdb" % Versions.kamonInfluxDb
      )
    )

    val kamonSystemMetrics: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "io.kamon" %% "kamon-system-metrics" % Versions.kamonSystemMetrics
      )
    )

    val kindProjector: Seq[Def.Setting[_]] = Def.settings(
      addCompilerPlugin("org.spire-math" %% "kind-projector" % Versions.kindProjector)
    )

    val logbackClassic: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "ch.qos.logback" % "logback-classic" % Versions.logback
      )
    )

    val slf4jApi: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "org.slf4j" % "slf4j-api" % Versions.slf4j
      )
    )

    val scalaTest: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % Versions.scalaTest % Test
      )
    )
  }
}
