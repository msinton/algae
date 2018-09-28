


## Algae
Algae is a [Scala][scala] library providing a diverse group of final tagless algebras.

> Algae ([/ˈældʒi, ˈælɡi/](https://en.wikipedia.org/wiki/Help:IPA/English); singular alga [/ˈælɡə/](https://en.wikipedia.org/wiki/Help:IPA/English)) is an informal term for a large, diverse group of [photosynthetic](https://en.wikipedia.org/wiki/Photosynthesis) [eukaryotic](https://en.wikipedia.org/wiki/Eukaryotic) [organisms](https://en.wikipedia.org/wiki/Organism) that are not necessarily closely related, and is thus [polyphyletic](https://en.wikipedia.org/wiki/Polyphyletic).

Algae is a new project under active development. Feedback and contributions are welcome.

### Introduction
Algae defines final tagless algebras around common capabilities, such as [`Logging`][logging] and [`Counting`][counting]. The core library makes use of [cats][cats], [cats-mtl][cats-mtl], and [cats-effect][cats-effect], while modules use external libraries to implement, complement, and define extra algebras. Algae also defines supportive constructs like: type classes, immutable data, and pure functions.

### Getting Started
To get started with [sbt][sbt], simply add the following lines to your `build.sbt` file.


```scala
val algaeVersion = "0.0.1"

resolvers += Resolver.bintrayRepo("ovotech", "maven")

libraryDependencies += "com.ovoenergy" %% "algae-core" % algaeVersion

// Module with laws for the type classes
libraryDependencies += "com.ovoenergy" %% "algae-laws" % algaeVersion

// Modules for the Counting algebra
libraryDependencies ++= Seq(
  "com.ovoenergy" %% "algae-kamon" % algaeVersion,
  "com.ovoenergy" %% "algae-kamon-influxdb" % algaeVersion,
  "com.ovoenergy" %% "algae-kamon-system-metrics" % algaeVersion
)

// Modules for the Logging algebra
libraryDependencies ++= Seq(
  "com.ovoenergy" %% "algae-slf4j" % algaeVersion,
  "com.ovoenergy" %% "algae-logback" % algaeVersion
)
```


[cats-effect]: https://typelevel.org/cats-effect/
[cats-mtl]: https://github.com/typelevel/cats-mtl
[cats]: https://typelevel.org/cats/
[counting]: core/src/main/scala/algae/Counting.scala
[logging]: core/src/main/scala/algae/Logging.scala
[sbt]: https://www.scala-sbt.org
[scala]: https://scala-lang.org/
