package algae.fs2

import cats.data.NonEmptyList
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import fs2.kafka.{CommittableMessage, ConsumerSettings, ProducerMessage, ProducerSettings, ProducerResult}

import scala.concurrent.ExecutionContext

package object kafka {
  def createDefaultKafkaConsumerStream[F[_], K, V](
    settings: ExecutionContext => ConsumerSettings[K, V]
  )(
    implicit F: ConcurrentEffect[F],
    context: ContextShift[F],
    timer: Timer[F]
  ): Stream[F, KafkaConsumer[F, K, V]] =
    fs2.kafka.consumerExecutionContextStream[F]
      .flatMap { executionContext =>
        createKafkaConsumerStream(settings(executionContext))
      }

  def createDefaultKafkaConsumerStream[F[_]](
    implicit F: ConcurrentEffect[F]
  ): DefaultKafkaConsumerStream[F] =
    new DefaultKafkaConsumerStream(F)

  def createKafkaConsumerStream[F[_], K, V](
    settings: ConsumerSettings[K, V]
  )(
    implicit F: ConcurrentEffect[F],
    context: ContextShift[F],
    timer: Timer[F]
  ): Stream[F, KafkaConsumer[F, K, V]] =
    fs2.kafka.consumerStream[F, K, V](settings).map { consumer =>
      new KafkaConsumer[F, K, V] {
        override val stream: Stream[F, CommittableMessage[F, K, V]] =
          consumer.stream

        override val partitionedStream: Stream[F, Stream[F, CommittableMessage[F, K, V]]] =
          consumer.partitionedStream

        override def subscribe(topics: NonEmptyList[String]): Stream[F, Unit] =
          consumer.subscribe(topics)
      }
    }

  def createKafkaConsumerStream[F[_]](
    implicit F: ConcurrentEffect[F]
  ): KafkaConsumerStream[F] =
    new KafkaConsumerStream(F)

  def createKafkaProducerStream[F[_], K, V](
    settings: ProducerSettings[K, V]
  )(
    implicit F: ConcurrentEffect[F]
  ): Stream[F, KafkaProducer[F, K, V]] =
    fs2.kafka.producerStream[F, K, V](settings).map { producer =>
      new KafkaProducer[F, K, V] {
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

  def createKafkaProducerStream[F[_]](
    implicit F: ConcurrentEffect[F]
  ): KafkaProducerStream[F] =
    new KafkaProducerStream(F)
}
