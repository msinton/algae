package algae.fs2.kafka

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import fs2.kafka.ConsumerSettings

import scala.concurrent.ExecutionContext

final class DefaultFs2KafkaConsumerStream[F[_]] private[kafka] (
  private val F: ConcurrentEffect[F]
) extends AnyVal {
  def using[K, V](
    settings: ExecutionContext => ConsumerSettings[K, V]
  )(
    implicit context: ContextShift[F],
    timer: Timer[F]
  ): Stream[F, Fs2KafkaConsumer[F, K, V]] =
    createDefaultFs2KafkaConsumerStream[F, K, V](settings)(F, context, timer)
}
