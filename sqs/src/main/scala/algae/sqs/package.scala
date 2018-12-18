package algae

import cats.effect.{Async, Concurrent, Resource}
import cats.syntax.functor._
import com.amazonaws.auth.{AWSCredentials, AWSStaticCredentialsProvider}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.Regions
import com.amazonaws.services.sqs.{AmazonSQSAsync, AmazonSQSAsyncClientBuilder}

package object sqs {

  def createSqsConsumer[F[_]](
    credentials: AWSCredentials,
    region: Regions,
    queueUrl: String
  )(
    implicit F: Concurrent[F]
  ): Resource[F, SqsConsumer[F]] =
    createSqsClient(credentials, region, queueUrl, _.build).map { sqsClient =>
      createSqsConsumer(sqsClient, queueUrl)
    }

  def createSqsProducer[F[_]](
    credentials: AWSCredentials,
    region: Regions,
    queueUrl: String
  )(
    implicit F: Concurrent[F]
  ): Resource[F, SqsProducer[F]] =
    createSqsClient(credentials, region, queueUrl, _.build).map { sqsClient =>
      new SqsProducerImpl(sqsClient, queueUrl)
    }

  def createSqsConsumer[F[_]](
    sqsClient: AmazonSQSAsync,
    queueUrl: String
  )(
    implicit F: Async[F]
  ): SqsConsumer[F] =
    new SqsConsumerImpl(sqsClient, queueUrl)

  def createSqsProducer[F[_]](
    sqsClient: AmazonSQSAsync,
    queueUrl: String
  )(
    implicit F: Async[F]
  ): SqsProducer[F] =
    new SqsProducerImpl(sqsClient, queueUrl)

  def createSqsClient[F[_]](
    credentials: AWSCredentials,
    region: Regions,
    queueUrl: String,
    configureBuilder: AmazonSQSAsyncClientBuilder => AmazonSQSAsync
  )(
    implicit F: Concurrent[F]
  ): Resource[F, AmazonSQSAsync] = {
    Resource.make {
      F.delay {
        configureBuilder(
          AmazonSQSAsyncClientBuilder
            .standard()
            .withCredentials(
              new AWSStaticCredentialsProvider(credentials)
            )
            .withEndpointConfiguration(
              new EndpointConfiguration(
                queueUrl,
                region.getName
              )
            )
        )
      }
    } { sqsClient =>
      F.start(F.delay(sqsClient.shutdown()))
        .map(_.join)
        .void
    }
  }
}
