package algae.fs2.kafka

import cats.effect.ConcurrentEffect
import fs2.Stream
import fs2.kafka.ProducerSettings

final class KafkaProducerStream[F[_]] private[kafka] (
  private val F: ConcurrentEffect[F]
) extends AnyVal {
  def using[K, V](
    settings: ProducerSettings[K, V]
  ): Stream[F, KafkaProducer[F, K, V]] =
    createKafkaProducerStream[F, K, V](settings)(F)
}
