package algae

import cats.effect.{Resource, Sync}
import cats.syntax.functor._
import com.amazonaws.auth.{AWSCredentials, AWSStaticCredentialsProvider}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.regions.Regions
import com.amazonaws.services.sqs.AmazonSQSClientBuilder

package object sqs {
  def createDefaultSqsConsumer[F[_]](
    credentials: AWSCredentials,
    region: Regions,
    queueUrl: String
  )(
    implicit F: Sync[F]
  ): Resource[F, SqsConsumer[F]] = {
    Resource(
      F.delay {
        AmazonSQSClientBuilder
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
      }.map(amazonSqs => (
        SqsConsumerImpl(amazonSqs, queueUrl),
        F.delay(amazonSqs.shutdown())
      ))
    )
  }

  def createDefaultSqsProducer[F[_]](
    credentials: AWSCredentials,
    region: Regions,
    queueUrl: String
  )(
    implicit F: Sync[F]
  ): Resource[F, SqsProducer[F]] = {
    Resource(
      F.delay {
        AmazonSQSClientBuilder
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
      }.map(amazonSqs => (
        SqsProducerImpl(amazonSqs, queueUrl),
        F.delay(amazonSqs.shutdown())
      ))
    )
  }
}
