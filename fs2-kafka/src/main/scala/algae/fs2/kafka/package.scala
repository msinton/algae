package algae.fs2

import cats.data.NonEmptyList
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import fs2.kafka._

import scala.concurrent.ExecutionContext

package object kafka {
  def createDefaultFs2KafkaConsumerStream[F[_], K, V](
    settings: ExecutionContext => ConsumerSettings[K, V]
  )(
    implicit F: ConcurrentEffect[F],
    context: ContextShift[F],
    timer: Timer[F]
  ): Stream[F, Fs2KafkaConsumer[F, K, V]] =
    consumerExecutionContextStream[F].flatMap { executionContext =>
      createFs2KafkaConsumerStream(settings(executionContext))
    }

  def createDefaultFs2KafkaConsumerStream[F[_]](
    implicit F: ConcurrentEffect[F]
  ): DefaultFs2KafkaConsumerStream[F] =
    new DefaultFs2KafkaConsumerStream(F)

  def createFs2KafkaConsumerStream[F[_], K, V](
    settings: ConsumerSettings[K, V]
  )(
    implicit F: ConcurrentEffect[F],
    context: ContextShift[F],
    timer: Timer[F]
  ): Stream[F, Fs2KafkaConsumer[F, K, V]] =
    consumerStream[F, K, V](settings).map { consumer =>
      new Fs2KafkaConsumer[F, K, V] {
        override val stream: Stream[F, CommittableMessage[F, K, V]] =
          consumer.stream

        override val partitionedStream: Stream[F, Stream[F, CommittableMessage[F, K, V]]] =
          consumer.partitionedStream

        override def subscribe(topics: NonEmptyList[String]): Stream[F, Unit] =
          consumer.subscribe(topics)
      }
    }

  def createFs2KafkaConsumerStream[F[_]](
    implicit F: ConcurrentEffect[F]
  ): Fs2KafkaConsumerStream[F] =
    new Fs2KafkaConsumerStream(F)

  def createFs2KafkaProducerStream[F[_], K, V](
    settings: ProducerSettings[K, V]
  )(
    implicit F: ConcurrentEffect[F]
  ): Stream[F, Fs2KafkaProducer[F, K, V]] =
    producerStream[F, K, V](settings).map { producer =>
      new Fs2KafkaProducer[F, K, V] {
        override def produceBatched[P](
          message: ProducerMessage[K, V, P]
        ): F[F[ProducerResult[K, V, P]]] =
          producer.produceBatched(message)

        override def produce[P](
          message: ProducerMessage[K, V, P]
        ): F[ProducerResult[K, V, P]] =
          producer.produce(message)
      }
    }

  def createFs2KafkaProducerStream[F[_]](
    implicit F: ConcurrentEffect[F]
  ): Fs2KafkaProducerStream[F] =
    new Fs2KafkaProducerStream(F)
}
