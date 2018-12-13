package algae.sqs

import com.amazonaws.services.sqs.model.SendMessageResult

trait SqsProducer[F[_]] {
  def send(message: String): F[SendMessageResult]
}
