package algae.fs2.kafka

import cats.effect.ConcurrentEffect
import fs2.Stream
import fs2.kafka.ProducerSettings

final class Fs2KafkaProducerStream[F[_]] private[kafka] (
  private val F: ConcurrentEffect[F]
) extends AnyVal {
  def using[K, V](
    settings: ProducerSettings[K, V]
  ): Stream[F, Fs2KafkaProducer[F, K, V]] =
    createFs2KafkaProducerStream[F, K, V](settings)(F)
}
