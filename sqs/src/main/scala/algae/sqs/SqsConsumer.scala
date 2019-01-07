package algae.sqs

import com.amazonaws.services.sqs.model.Message
import fs2.Stream

trait SqsConsumer[F[_]] {
  def messages: Stream[F, Message]
  def commit(receiptHandle: String): F[Unit]
}

