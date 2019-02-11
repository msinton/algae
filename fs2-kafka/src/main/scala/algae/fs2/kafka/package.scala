package algae.fs2

import cats.Reducible
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import fs2.kafka._

package object kafka {
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

        override def subscribe[G[_]](topics: G[String])(implicit G: Reducible[G]): F[Unit] =
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
        override def produce[G[+ _], P](
          message: ProducerMessage[G, K, V, P]
        ): F[F[ProducerResult[G, K, V, P]]] =
          producer.produce(message)

        override def producePassthrough[G[+ _], P](
          message: ProducerMessage[G, K, V, P]
        ): F[F[P]] =
          producer.producePassthrough(message)
      }
    }

  def createFs2KafkaProducerStream[F[_]](
    implicit F: ConcurrentEffect[F]
  ): Fs2KafkaProducerStream[F] =
    new Fs2KafkaProducerStream(F)
}
