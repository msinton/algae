package algae.fs2.kafka

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import fs2.kafka.ConsumerSettings

import scala.concurrent.ExecutionContext

final class DefaultKafkaConsumerStream[F[_]] private[kafka] (
  private val F: ConcurrentEffect[F]
) extends AnyVal {
  def using[K, V](
    settings: ExecutionContext => ConsumerSettings[K, V]
  )(
    implicit context: ContextShift[F],
    timer: Timer[F]
  ): Stream[F, KafkaConsumer[F, K, V]] =
    createDefaultKafkaConsumerStream[F, K, V](settings)(F, context, timer)
}
