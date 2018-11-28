package algae.fs2.kafka

import algae.KafkaConsumer
import cats.data.NonEmptyList
import fs2.Stream
import fs2.kafka.CommittableMessage

trait Fs2KafkaConsumer[F[_], K, V]
    extends KafkaConsumer[Stream, CommittableMessage, F, NonEmptyList, String, K, V]
