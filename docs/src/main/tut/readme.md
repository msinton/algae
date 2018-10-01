```tut:invisible
import algae.build.info._
```

## Algae
Algae is a [Scala][scala] library providing a diverse group of final tagless algebras.

> Algae ([/ˈældʒi, ˈælɡi/](https://en.wikipedia.org/wiki/Help:IPA/English); singular alga [/ˈælɡə/](https://en.wikipedia.org/wiki/Help:IPA/English)) is an informal term for a large, diverse group of [photosynthetic](https://en.wikipedia.org/wiki/Photosynthesis) [eukaryotic](https://en.wikipedia.org/wiki/Eukaryotic) [organisms](https://en.wikipedia.org/wiki/Organism) that are not necessarily closely related, and is thus [polyphyletic](https://en.wikipedia.org/wiki/Polyphyletic).

Algae is a new project under active development. Feedback and contributions are welcome.

### Introduction
Algae defines final tagless algebras around common capabilities, such as [`Logging`][logging] and [`Counting`][counting]. The core library makes use of [cats][cats], [cats-mtl][cats-mtl], and [cats-effect][cats-effect], while modules use external libraries to implement, complement, and define extra algebras. Algae also defines supportive constructs like: type classes, immutable data, and pure functions.

### Getting Started
To get started with [sbt][sbt], simply add the following lines to your `build.sbt` file.

```tut:passthrough
println(
s"""
 |```scala
 |val algaeVersion = "$latestVersion"
 |
 |resolvers += Resolver.bintrayRepo("ovotech", "maven")
 |
 |libraryDependencies += "com.ovoenergy" %% "algae-core" % algaeVersion
 |
 |// Module with laws for the type classes
 |libraryDependencies += "com.ovoenergy" %% "algae-laws" % algaeVersion
 |
 |// Modules for the Counting algebra
 |libraryDependencies ++= Seq(
 |  "com.ovoenergy" %% "algae-kamon" % algaeVersion,
 |  "com.ovoenergy" %% "algae-kamon-influxdb" % algaeVersion,
 |  "com.ovoenergy" %% "algae-kamon-system-metrics" % algaeVersion
 |)
 |
 |// Modules for the Config algebra
 |libraryDependencies ++= Seq(
 |  "com.ovoenergy" %% "algae-ciris" % algaeVersion,
 |  "com.ovoenergy" %% "algae-ciris-kubernetes" % algaeVersion
 |)
 |```
 """.stripMargin.trim
)
```

### Logging
The `Logging` algebra implements log accumulation and dispatching of log messages, with support for diagnostic contexts. This is done via the `MonadLog` type class, which is a thin wrapper around `MonadState`, with additional laws governing log accumulation. If you're working with a `Sync[F]` context, a `Ref[F]` can be used to implement `MonadLog`, and there's a `createMonadLog` helper function for exactly that.

If you want slf4j logging support, simply add the `algae-slf4j` module to your dependencies in `build.sbt`.  
The `algae-logback` module adds Logback as a dependency, for convenience when wanting to use Logback.

```tut:passthrough
println(
s"""
 |```scala
 |libraryDependencies ++= Seq(
 |  "com.ovoenergy" %% "algae-slf4j" % algaeVersion,
 |  "com.ovoenergy" %% "algae-logback" % algaeVersion
 |)
 |```
 """
)
```

#### Getting Started

Start by defining your log entries. It's recommended to use a coproduct, like a `sealed trait`. This keeps your log entries in a single place, and the logic for what is logged is encapsulated in the log entries. A log entry consists of a `LogLevel` and a `String` message. We define an instance for `LogEntry` for our coproduct, to define it as a log entry.

```tut:silent
import algae._
import algae.logging._

object entries {
  sealed trait Log {
    def level: LogLevel
    def message: String
  }

  case object ApplicationStarted extends Log {
    def level = LogLevel.Info
    def message = "Application started"
  }

  case object HelloWorld extends Log {
    def level = LogLevel.Info
    def message = "Hello, world"
  }

  implicit val logLogEntry: LogEntry[Log] =
    LogEntry.from(_.level, _.message)
}
```

We do the same for the diagnostic context, by also defining a coproduct as a `sealed abstract class`. We implement an instance of `MdcEntry` for our coproduct, to define it as an entry for diagnostic contexts.

```tut:silent
object mdc {
  sealed abstract class Mdc(val key: String, val value: String)

  final case class TraceToken(override val value: String) extends Mdc("traceToken", value)

  implicit val mdcMdcEntry: MdcEntry[Mdc] =
    MdcEntry.from(_.key, _.value)
}
```

To create an instance of `Logging`, we'll need to have a `MonadLog` instance. We'll use `IO` and create an instance from a `Ref`, using `createMonadLog`. We need to chose a collection type, and `Chain` is a good default choice here because it supports constant-time append.

The `algae-slf4j` module defines a `createLogging` function which creates a `Logging` instance given a `MonadLog` instance, and dispatches log messages using slf4j bindings. We can use `log` to accumulate log entries, and later dispatch them with `dispatchLogs`. Using `logNow` we can immediately dispatch the given log entries as a message.

```tut:silent
import algae.slf4j._
import cats.data.Chain
import cats.effect.IO
import entries._
import mdc._

for {
  logs <- createMonadLog[IO, Chain[Log]]
  logging <- createLogging[IO, Chain, Log, Mdc]("App", logs)
  _ <- logging.logNow(ApplicationStarted)
  _ <- logging.log(HelloWorld)
  _ <- logging.log(HelloWorld)
  _ <- logging.dispatchLogs
} yield ()
```

The example above immediately logs `ApplicationStarted` and then logs a combined message containing `HelloWorld` twice. After dispatching logs with `dispatchLogs`, accumulated logs are cleared. It's worth noting that the log entries are stored in a separate `Ref`, so even if part of your program fails, any logged messages are still available.

[cats-effect]: https://typelevel.org/cats-effect/
[cats-mtl]: https://github.com/typelevel/cats-mtl
[cats]: https://typelevel.org/cats/
[counting]: core/src/main/scala/algae/Counting.scala
[logging]: core/src/main/scala/algae/Logging.scala
[sbt]: https://www.scala-sbt.org
[scala]: https://scala-lang.org/
