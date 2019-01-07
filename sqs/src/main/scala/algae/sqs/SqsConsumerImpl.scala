package algae.sqs

import cats.effect.Async
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model._
import fs2._

import scala.collection.JavaConverters._

private[this] final class SqsConsumerImpl[F[_]](
  sqs: AmazonSQSAsync,
  queueUrl: String
)(
  implicit F: Async[F]
) extends SqsConsumer[F] {

  def messages: Stream[F, Message] =
    Stream.repeatEval(request).flatMap(Stream.chunk)

  private def request: F[Chunk[Message]] =
    F.async { cb =>
      sqs.receiveMessageAsync(
        queueUrl,
        new AsyncHandler[ReceiveMessageRequest, ReceiveMessageResult] {
          override def onSuccess(
            request: ReceiveMessageRequest,
            result: ReceiveMessageResult
          ): Unit = cb(Right(Chunk.buffer(result.getMessages.asScala)))

          override def onError(
            error: Exception
          ): Unit = cb(Left(error))
        }
      )

      ()
    }

  def commit(receiptHandle: String): F[Unit] =
    F.async { cb =>
      sqs.deleteMessageAsync(
        queueUrl,
        receiptHandle,
        new AsyncHandler[DeleteMessageRequest, DeleteMessageResult] {
          override def onSuccess(
            request: DeleteMessageRequest,
            result: DeleteMessageResult
          ): Unit = cb(Right(()))

          override def onError(
            error: Exception
          ): Unit = cb(Left(error))
        }
      )

      ()
    }
}
