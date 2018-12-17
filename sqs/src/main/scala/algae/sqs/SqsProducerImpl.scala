package algae.sqs

import cats.effect.Async
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.{SendMessageRequest, SendMessageResult}

private[this] final class SqsProducerImpl[F[_]](
  sqs: AmazonSQSAsync,
  queueUrl: String
)(
  implicit F: Async[F],
) extends SqsProducer[F] {

  override def send(message: String): F[SendMessageResult] =
    F.async { cb =>
      sqs.sendMessageAsync(
        queueUrl,
        message,
        new AsyncHandler[SendMessageRequest, SendMessageResult] {
          override def onSuccess(
            request: SendMessageRequest,
            result: SendMessageResult
          ): Unit = cb(Right(result))

          override def onError(
            error: Exception
          ): Unit = cb(Left(error))
        }
      )

      ()
    }
}
