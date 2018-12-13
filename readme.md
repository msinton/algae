


## Algae
Algae is a [Scala][scala] library providing a diverse group of final tagless algebras.

> Algae ([/ˈældʒi, ˈælɡi/](https://en.wikipedia.org/wiki/Help:IPA/English); singular alga [/ˈælɡə/](https://en.wikipedia.org/wiki/Help:IPA/English)) is an informal term for a large, diverse group of [photosynthetic](https://en.wikipedia.org/wiki/Photosynthesis) [eukaryotic](https://en.wikipedia.org/wiki/Eukaryotic) [organisms](https://en.wikipedia.org/wiki/Organism) that are not necessarily closely related, and is thus [polyphyletic](https://en.wikipedia.org/wiki/Polyphyletic).

Algae is a new project under active development. Feedback and contributions are welcome.

### Introduction
Algae defines final tagless algebras around common capabilities, such as [`Logging`](#logging) and [`Counting`](#counting). The core library makes use of [cats][cats], [cats-mtl][cats-mtl], and [cats-effect][cats-effect], while modules use external libraries to implement, complement, and define extra algebras. Algae also defines supportive constructs like: type classes, immutable data, and pure functions.

### Contents
1. [Getting Started](#getting-started)
1. [Configuration](#configuration)
1. [Counting](#counting)
1. [Kafka](#kafka)
1. [Logging](#logging)
1. [Sqs](#sqs)

### Getting Started
To get started with [sbt][sbt], simply add the following lines to your `build.sbt` file.


```scala
val algaeVersion = "0.1.7"

resolvers += Resolver.bintrayRepo("ovotech", "maven")

libraryDependencies += "com.ovoenergy" %% "algae-core" % algaeVersion
```


### Configuration
The `Config` algebra implements basic configuration using environment variables and system properties. An implementation using Ciris is defined as `CirisConfig`, and there's a `KubernetesConfig` algebra for Kubernetes secrets support. There is also an `AivenKafkaConfig` algebra using [ciris-aiven-kafka](https://github.com/ovotech/ciris-aiven-kafka). Following are the relevant modules and the lines you need for `build.sbt` to include them in your project.



```scala
libraryDependencies ++= Seq(
  "com.ovoenergy" %% "algae-ciris" % algaeVersion,
  "com.ovoenergy" %% "algae-ciris-kubernetes" % algaeVersion,
  "com.ovoenergy" %% "algae-ciris-aiven-kafka" % algaeVersion
)
```
 


To create an instance of `CirisConfig`, simply `import algae.ciris._` and use `createCirisConfig`.

```scala
import algae.ciris._
import cats.MonadError
import cats.effect.IO
import ciris.loadConfig
import ciris.cats.effect._

final case class Config(appEnv: String, maxRetries: Int)

def loadConfiguration[F[_]](config: CirisConfig[F])(
  implicit F: MonadError[F, Throwable]
): F[Config] = {
  loadConfig(
    config.env[String]("APP_ENV"),
    config.prop[Int]("max.retries")
  )(Config.apply).orRaiseThrowable
}

val cirisConfig = createCirisConfig[IO]
loadConfiguration(cirisConfig)
```

To create an instance of `KubernetesConfig`, simply `import algae.ciris.kubernetes._` and use either:

- `createDefaultKubernetesConfig` to use the default configuration options, or 
- `createKubernetesConfig` to customize the API client and authenticators to use.

```scala
import algae.ciris.kubernetes._
import cats.effect.ContextShift
import ciris.Secret

implicit val contextShift: ContextShift[IO] =
  IO.contextShift(scala.concurrent.ExecutionContext.global)

final case class Config(appEnv: String, maxRetries: Int, apiKey: Secret[String])

def loadConfiguration[F[_]](config: KubernetesConfig[F])(
  implicit F: MonadError[F, Throwable]
): F[Config] = {
  loadConfig(
    config.env[String]("APP_ENV"),
    config.prop[Int]("max.retries"),
    config.secret[Secret[String]]("namespace", "api-key")
  )(Config.apply).orRaiseThrowable
}

createDefaultKubernetesConfig[IO].
  flatMap(loadConfiguration[IO])
```

To create an instance of `AivenKafkaConfig`, simply `import algae.ciris.aiven.kafka._` and use `createAivenKafkaConfig`.

```scala
import algae.ciris.aiven.kafka._

val aivenKafkaConfig: AivenKafkaConfig[IO] =
  createAivenKafkaConfig[IO]
```

### Counting
The `Counting` algebra implements counting and count accumulation. Accumulation is supported via the `MonadLog` type class, and `createMonadLog` is available to create a `MonadLog` using a `Ref[F]`. An implementation of `Counting` using `Kamon` is provided. If count accumulation isn't necessary, `CountingNow` can be used. Additionally, there are functions for configuring Kamon modules available.



```scala
libraryDependencies ++= Seq(
  "com.ovoenergy" %% "algae-kamon" % algaeVersion,
  "com.ovoenergy" %% "algae-kamon-influxdb" % algaeVersion,
  "com.ovoenergy" %% "algae-kamon-system-metrics" % algaeVersion
)
```
 


Start by defining your counter increments. It's recommended to use a coproduct, like a `sealed abstract class`. This keeps your counter increments in a single place, and the logic for what is counted is encapsulated in the increments. A `CounterIncrement` consists of a counter name, a map of tags, and the number of times to increment.

```scala
import algae.counting._

object counts {
  sealed abstract class Count(
    val counterName: String,
    val tags: Map[String, String],
    val times: Long
  )

  case object ApplicationStarted extends
    Count("application_started", Map.empty, 1L)

  final case class SaidHello(override val times: Long) extends
    Count("said_hello", Map.empty, times)

  implicit val countCounterIncrement: CounterIncrement[Count] =
    CounterIncrement.from(_.counterName, _.tags, _.times)
}
```

To create an instance of `Counting`, we'll need to have a `MonadLog` instance. We'll use `IO` and create an instance from a `Ref`, using `createMonadLog`. We need to choose a collection type, and `Chain` is a good default choice here because it supports constant-time append. We also setup reporting and system metrics collection using provided functions.

```scala
import algae._
import algae.kamon._
import algae.kamon.influxdb._
import algae.kamon.system._
import cats.data.Chain
import counts._

val kamonSetup =
  influxDbRegistration[IO].
    flatMap(_ => systemMetricsCollection[IO])

kamonSetup.use { _ =>
  for {
    counts <- createMonadLog[IO, Chain[Count]]
    counting = createCounting(counts)
    _ <- counting.countNow(ApplicationStarted)
    _ <- counting.count(SaidHello(1L))
    _ <- counting.count(SaidHello(1L))
    _ <- counting.dispatchCounts
  } yield ()
}
```

The example above immediately counts `ApplicationStarted` and then counts `SaidHello` twice when `dispatchCounts` is invoked. After dispatching counts with `dispatchCounts`, accumulated counts are cleared. It's worth noting that the counter increments are stored in a separate `Ref`, so even if part of your program fails, any counts are still available.

### Kafka
The `algae-fs2-kafka` module provides `KafkaConsumer` and `KafkaProducer` algebras and implements them using [fs2-kafka][fs2-kafka].



```scala
libraryDependencies += "com.ovoenergy" %% "algae-fs2-kafka" % algaeVersion
```
 


To create a `KafkaConsumer`, you can use these functions:

- `createKafkaConsumerStream[F, K, V](settings)`, or

- `createKafkaConsumerStream[F].using(settings)`,

or one of the following, where a new default `ExecutionContext` will be created.

- `createDefaultKafkaConsumerStream[F, K, V](settings)`, or

- `createDefaultKafkaConsumerStream[F].using(settings)`.

To create a `KafkaProducer`, use one of the following functions.

- `createKafkaProducerStream[F, K, V](settings)`, or

- `createKafkaProducerStream[F].using(settings)`.

Following is an example of how to create a `KafkaConsumer` and `KafkaProducer`.

```scala
import cats.data.NonEmptyList
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import cats.syntax.traverse._
import algae.fs2.kafka._
import _root_.fs2.kafka._
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val consumerSettings = (executionContext: ExecutionContext) =>
      ConsumerSettings(
        keyDeserializer = new StringDeserializer,
        valueDeserializer = new StringDeserializer,
        executionContext = executionContext
      )
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost")
      .withGroupId("group")

    val producerSettings =
      ProducerSettings(
        keySerializer = new StringSerializer,
        valueSerializer = new StringSerializer,
      )
      .withBootstrapServers("localhost")

    val topics =
      NonEmptyList.one("topic")

    def processRecord(record: ConsumerRecord[String, String]): IO[(String, String)] =
      IO.pure(record.key -> record.value)

    val stream =
      for {
        consumer <- createDefaultKafkaConsumerStream[IO].using(consumerSettings)
        producer <- createKafkaProducerStream[IO].using(producerSettings)
        _ <- consumer.subscribe(topics)
        _ <- consumer.stream
          .mapAsync(25)(message =>
            processRecord(message.record)
              .map {
                case (key, value) =>
                  val record = new ProducerRecord("topic", key, value)
                  ProducerMessage.single(record, message.committableOffset)
              })
            .evalMap(producer.produceBatched)
            .map(_.map(_.passthrough))
            .to(commitBatchWithinF(500, 15.seconds))
      } yield ()

    stream.compile.drain.as(ExitCode.Success)
  }
}
```

### Logging
The `Logging` algebra implements log accumulation and dispatching of log messages, with support for diagnostic contexts. This is done via the `MonadLog` type class, which is a thin wrapper around `MonadState`, with additional laws governing log accumulation. If you're working with a `Sync[F]` context, a `Ref[F]` can be used to implement `MonadLog`, and there's a `createMonadLog` helper function for exactly that. If log accumulation isn't necessary, `LoggingNow` can be used.

If you want slf4j logging support, simply add the `algae-slf4j` module to your dependencies in `build.sbt`.  
The `algae-logback` module adds Logback as a dependency, for convenience when wanting to use Logback.



```scala
libraryDependencies ++= Seq(
  "com.ovoenergy" %% "algae-slf4j" % algaeVersion,
  "com.ovoenergy" %% "algae-logback" % algaeVersion
)
```
 


Start by defining your log entries. It's recommended to use a coproduct, like a `sealed trait`. This keeps your log entries in a single place, and the logic for what is logged is encapsulated in the log entries. A log entry consists of a `LogLevel` and a `String` message. We define an instance for `LogEntry` for our coproduct, to define it as a log entry.

```scala
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

```scala
object mdc {
  sealed abstract class Mdc(val key: String, val value: String)

  final case class TraceToken(override val value: String) extends Mdc("traceToken", value)

  implicit val mdcMdcEntry: MdcEntry[Mdc] =
    MdcEntry.from(_.key, _.value)
}
```

To create an instance of `Logging`, we'll need to have a `MonadLog` instance. We'll use `IO` and create an instance from a `Ref`, using `createMonadLog`. We need to choose a collection type, and `Chain` is a good default choice here because it supports constant-time append.

The `algae-slf4j` module defines a `createLogging` function which creates a `Logging` instance given a `MonadLog` instance, and dispatches log messages using slf4j bindings. We can use `log` to accumulate log entries, and later dispatch them with `dispatchLogs`. Using `logNow` we can immediately dispatch the given log entries as a message.

```scala
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

### Sqs
The `Sqs` module provides the `SqsConsumer` and `SqsProducer` algebras.



```scala
libraryDependencies ++= Seq(
  "com.ovoenergy" %% "algae-sqs" % algaeVersion
)
```
 


Here an example of how to use the `SqsProducer` and `SqsConsumer`.

```scala
import algae.sqs._
import cats.effect.{ExitCode, IO, IOApp}
import cats.instances.list._
import cats.syntax.traverse._
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.sqs.model.SendMessageResult
import scala.concurrent.duration._

object Main extends IOApp {
  val credentials = new BasicAWSCredentials(
    "ACCESS_KEY_ID",
    "SECRET_ACCESS_KEY"
  )
  
  val region: Regions = Regions.EU_WEST_1
  
  val queueUrl = "https://sqs.us-east-2.amazonaws.com/123456789012/MyQueue"
  
  val producer = createDefaultSqsProducer[IO](
    credentials,
    region,
    queueUrl
  )
   
  val consumer = createDefaultSqsConsumer[IO](
    credentials,
    region,
    queueUrl
  )
      
  override def run(args: List[String]): IO[ExitCode] = producer.use { producer =>
    consumer.use { consumer =>
      for {
        _ <- producer.send("MyMessage")
        messages <- consumer
          .messages
          .groupWithin(Int.MaxValue, 1.second)
          .compile
          .toList
          .map(_.flatMap(_.toList))
        _ <- messages.traverse[IO, Unit](message => consumer.commit(message.getReceiptHandle))
      } yield ExitCode.Success
    }
  }
}
```



[cats-effect]: https://typelevel.org/cats-effect
[cats-mtl]: https://github.com/typelevel/cats-mtl
[cats]: https://typelevel.org/cats
[fs2-kafka]: https://github.com/ovotech/fs2-kafka
[sbt]: https://www.scala-sbt.org
[scala]: https://scala-lang.org
[sqs]: https://aws.amazon.com/sqs/
