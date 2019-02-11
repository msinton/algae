package algae.fs2.kafka

import cats.Reducible
import fs2.Stream
import fs2.kafka.CommittableMessage

trait Fs2KafkaConsumer[F[_], K, V] {
  def stream: Stream[F, CommittableMessage[F, K, V]]

  def partitionedStream: Stream[F, Stream[F, CommittableMessage[F, K, V]]]

  def subscribe[G[_]](topics: G[String])(implicit G: Reducible[G]): F[Unit]
}
