package algae.sqs

import cats.effect.Sync
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.{SendMessageRequest, SendMessageResult}

final case class SqsProducerImpl[F[_]](
  sqs: AmazonSQS,
  queueUrl: String
)(
  implicit F: Sync[F]
) extends SqsProducer[F] {

  override def send(message: String): F[SendMessageResult] =
    F.delay {
      sqs.sendMessage(
        new SendMessageRequest(
          queueUrl,
          message
        )
      )
    }
}
