import sbt.Keys._
import sbt._

object Dependencies extends AutoPlugin {
  object Versions {
    val cats = "1.5.0"
    val catsMtl = "0.4.0"
    val catsEffect = "1.2.0"
    val ciris = "0.12.1"
    val cirisAivenKafka = "0.13"
    val cirisKubernetes = "0.10"
    val fs2 = "1.0.3"
    val fs2Kafka = "0.19.0"
    val kamon = "1.1.5"
    val kamonInfluxDb = "1.0.2"
    val kamonSystemMetrics = "1.0.1"
    val kindProjector = "0.9.9"
    val logback = "1.2.3"
    val scalaTest = "3.0.5"
    val slf4j = "1.7.25"
    val awsSdk = "1.11.490"
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

    val cirisAivenKafka: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "com.ovoenergy" %% "ciris-aiven-kafka" % Versions.cirisAivenKafka
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

    val awsSqs: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "com.amazonaws" % "aws-java-sdk-sqs" % Versions.awsSdk,
        "co.fs2" %% "fs2-core" % Versions.fs2
      )
    )

    val scalaTest: Seq[Def.Setting[_]] = Def.settings(
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % Versions.scalaTest % Test
      )
    )
  }
}
