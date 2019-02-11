package algae.fs2.kafka

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import fs2.kafka.ConsumerSettings

final class Fs2KafkaConsumerStream[F[_]] private[kafka] (
  private val F: ConcurrentEffect[F]
) extends AnyVal {
  def using[K, V](settings: ConsumerSettings[K, V])(
    implicit context: ContextShift[F],
    timer: Timer[F]
  ): Stream[F, Fs2KafkaConsumer[F, K, V]] =
    createFs2KafkaConsumerStream[F, K, V](settings)(F, context, timer)
}
