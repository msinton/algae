package algae.sqs

import cats.effect.Sync
import cats.syntax.functor._
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.{Message, ReceiveMessageRequest}
import fs2._

import scala.collection.JavaConverters._

final case class SqsConsumerImpl[F[_]](
  sqs: AmazonSQS,
  queueUrl: String
)(
  implicit F: Sync[F]
) extends SqsConsumer[F] {

  def messages: Stream[F, Message] =
    Stream.repeatEval(request).flatMap(Stream.chunk)

  private def request: F[Chunk[Message]] =
    F.delay {
        sqs
          .receiveMessage(
            new ReceiveMessageRequest()
              .withQueueUrl(queueUrl)
          )
          .getMessages
      }
      .map(messages => Chunk(messages.asScala: _*))

  def commit(receiptHandle: String): F[Unit] =
    F.delay {
      sqs.deleteMessage(queueUrl, receiptHandle)
    }.void
}


