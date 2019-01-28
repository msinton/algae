package algae.fs2

import cats.Reducible
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import fs2.kafka._

package object kafka {
  def createKafkaConsumerStream[F[_], K, V](
    settings: ConsumerSettings[K, V]
  )(
    implicit F: ConcurrentEffect[F],
    context: ContextShift[F],
    timer: Timer[F]
  ): Stream[F, algae.fs2.kafka.KafkaConsumer[F, K, V]] =
    fs2.kafka.consumerStream[F, K, V](settings).map { consumer =>
      new KafkaConsumer[F, K, V] {
        override val stream: Stream[F, CommittableMessage[F, K, V]] =
          consumer.stream

        override val partitionedStream: Stream[F, Stream[F, CommittableMessage[F, K, V]]] =
          consumer.partitionedStream

        override def subscribe[G[_]](topics: G[String])(implicit G: Reducible[G]): F[Unit] =
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
  ): Stream[F, algae.fs2.kafka.KafkaProducer[F, K, V]] =
    fs2.kafka.producerStream[F, K, V](settings).map { producer =>
      new KafkaProducer[F, K, V] {
        override def produce[G[+_], P](
          message: ProducerMessage[G, K, V, P]
        ): F[F[ProducerResult[G, K, V, P]]] =
          producer.produce(message)

        override def producePassthrough[G[+_], P](
          message: ProducerMessage[G, K, V, P]
        ): F[F[P]] =
          producer.producePassthrough(message)
      }
    }

  def createKafkaProducerStream[F[_]](
    implicit F: ConcurrentEffect[F]
  ): KafkaProducerStream[F] =
    new KafkaProducerStream(F)
}
