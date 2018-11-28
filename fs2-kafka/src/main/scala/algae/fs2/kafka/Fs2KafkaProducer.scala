package algae.fs2.kafka

import algae.KafkaProducer
import fs2.kafka.{ProducerMessage, ProducerResult}

trait Fs2KafkaProducer[F[_], K, V] extends KafkaProducer[F, ProducerMessage, ProducerResult, K, V]
