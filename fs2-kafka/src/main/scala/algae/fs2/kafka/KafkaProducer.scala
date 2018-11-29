package algae.fs2.kafka

import fs2.kafka.{ProducerMessage, ProducerResult}

trait KafkaProducer[F[_], K, V] {
  def produceBatched[P](message: ProducerMessage[K, V, P]): F[F[ProducerResult[K, V, P]]]

  def produce[P](message: ProducerMessage[K, V, P]): F[ProducerResult[K, V, P]]
}
