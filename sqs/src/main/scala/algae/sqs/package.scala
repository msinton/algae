package algae

import cats.effect.{Async, Concurrent, Resource}
import cats.syntax.flatMap._
import com.amazonaws.auth.{
  AWSCredentials,
  AWSCredentialsProvider,
  AWSStaticCredentialsProvider,
  BasicAWSCredentials
}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.Regions
import com.amazonaws.services.sqs.{AmazonSQSAsync, AmazonSQSAsyncClientBuilder}

package object sqs {

  def createSqsConsumer[F[_]](
    credentialsProvider: AWSCredentialsProvider,
    region: Regions,
    queueUrl: String
  )(
    implicit F: Concurrent[F]
  ): Resource[F, SqsConsumer[F]] =
    createSqsClient(credentialsProvider, region, queueUrl)
      .map { sqsClient =>
        createSqsConsumer(sqsClient, queueUrl)
      }

  def createSqsConsumer[F[_]](
    credentials: AWSCredentials,
    region: Regions,
    queueUrl: String
  )(
    implicit F: Concurrent[F]
  ): Resource[F, SqsConsumer[F]] =
    createSqsClient(toCredentialsProvider(credentials), region, queueUrl)
      .map { sqsClient =>
        createSqsConsumer(sqsClient, queueUrl)
      }

  def createSqsProducer[F[_]](
    credentialsProvider: AWSCredentialsProvider,
    region: Regions,
    queueUrl: String
  )(
    implicit F: Concurrent[F]
  ): Resource[F, SqsProducer[F]] =
    createSqsClient(credentialsProvider, region, queueUrl)
      .map { sqsClient =>
        new SqsProducerImpl(sqsClient, queueUrl)
      }

  def createSqsProducer[F[_]](
    credentials: AWSCredentials,
    region: Regions,
    queueUrl: String
  )(
    implicit F: Concurrent[F]
  ): Resource[F, SqsProducer[F]] =
    createSqsClient(toCredentialsProvider(credentials), region, queueUrl)
      .map { sqsClient =>
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
  ): SqsProducer[F] = new SqsProducerImpl(sqsClient, queueUrl)

  def createSqsClient[F[_]](
    build: AmazonSQSAsyncClientBuilder => AmazonSQSAsync
  )(
    implicit F: Concurrent[F]
  ): Resource[F, AmazonSQSAsync] = {
    Resource.make {
      F.delay(build(AmazonSQSAsyncClientBuilder.standard()))
    } { sqsClient =>
      F.start(F.delay(sqsClient.shutdown())).flatMap(_.join)
    }
  }
  private[this] def createSqsClient[F[_]](
    credentialsProvider: AWSCredentialsProvider,
    region: Regions,
    queueUrl: String
  )(
    implicit F: Concurrent[F]
  ): Resource[F, AmazonSQSAsync] =
    createSqsClient(
      _.withCredentials(credentialsProvider)
        .withEndpointConfiguration(
          new EndpointConfiguration(
            queueUrl,
            region.getName
          )
        )
        .build()
    )

  private[this] def toCredentialsProvider(credentials: AWSCredentials): AWSCredentialsProvider =
    new AWSStaticCredentialsProvider(
      new BasicAWSCredentials(
        credentials.getAWSAccessKeyId,
        credentials.getAWSSecretKey
      )
    )
}
