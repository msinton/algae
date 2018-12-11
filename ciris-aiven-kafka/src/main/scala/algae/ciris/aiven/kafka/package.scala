package algae.ciris.aiven

import cats.effect.Sync
import ciris.ConfigResult
import ciris.aiven.kafka._
import ciris.cats.effect._

package object kafka {
  def createAivenKafkaConfig[F[_]](implicit F: Sync[F]): AivenKafkaConfig[F] =
    new AivenKafkaConfig[F] {
      override def setupAivenKafka(
        clientPrivateKey: ConfigResult[F, AivenKafkaClientPrivateKey],
        clientCertificate: ConfigResult[F, AivenKafkaClientCertificate],
        serviceCertificate: ConfigResult[F, AivenKafkaServiceCertificate]
      ): ConfigResult[F, AivenKafkaSetupDetails] =
        aivenKafkaSetup(
          clientPrivateKey = clientPrivateKey,
          clientCertificate = clientCertificate,
          serviceCertificate = serviceCertificate
        )
    }
}
