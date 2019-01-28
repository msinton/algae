package algae.fs2.kafka

import fs2.kafka.{ProducerMessage, ProducerResult}

trait KafkaProducer[F[_], K, V] {
  def produce[G[+_], P](
    message: ProducerMessage[G, K, V, P]
  ): F[F[ProducerResult[G, K, V, P]]]

 def producePassthrough[G[+_], P](
    message: ProducerMessage[G, K, V, P]
  ): F[F[P]]
}
