package algae

import cats.effect.{Async, Resource, Sync}
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
    implicit F: Async[F]
  ): Resource[F, SqsConsumer[F]] =
    createSqsClient(credentials, region, queueUrl)
      .flatMap { sqsClient =>
        Resource.pure(createConsumer(sqsClient, queueUrl))
      }

  def createSqsProducer[F[_]](
    credentials: AWSCredentials,
    region: Regions,
    queueUrl: String
  )(
    implicit F: Async[F]
  ): Resource[F, SqsProducer[F]] =
    createSqsClient(credentials, region, queueUrl)
      .flatMap { sqsClient =>
        Resource.pure(new SqsProducerImpl(sqsClient, queueUrl))
      }

  def createSqsConsumerAndProducer[F[_]](
    credentials: AWSCredentials,
    region: Regions,
    queueUrl: String
  )(
    implicit F: Async[F]
  ): Resource[F, (SqsConsumer[F], SqsProducer[F])] =
    createSqsClient(credentials, region, queueUrl)
      .flatMap { sqsClient =>
        Resource.pure((
          new SqsConsumerImpl(sqsClient, queueUrl),
          new SqsProducerImpl(sqsClient, queueUrl)
        ))
      }

  def createConsumer[F[_]](
    sqsClient: AmazonSQSAsync,
    queueUrl: String
  )(
    implicit F: Async[F]
  ): SqsConsumer[F] =
    new SqsConsumerImpl(sqsClient, queueUrl)

  def createProducer[F[_]](
    sqsClient: AmazonSQSAsync,
    queueUrl: String
  )(
    implicit F: Async[F]
  ): SqsProducer[F] =
    new SqsProducerImpl(sqsClient, queueUrl)

  def createSqsClient[F[_]](
    credentials: AWSCredentials,
    region: Regions,
    queueUrl: String
  )(
    implicit F: Sync[F]
  ): Resource[F, AmazonSQSAsync] = {
    Resource.make {
      F.delay {
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
          .build()
      }
    } { sqsClient =>
      F.delay(sqsClient.shutdown())
    }
  }
}
