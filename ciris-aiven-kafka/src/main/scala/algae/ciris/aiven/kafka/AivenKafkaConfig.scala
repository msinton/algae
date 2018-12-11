package algae.ciris.aiven.kafka

import ciris.ConfigResult
import ciris.aiven.kafka._

trait AivenKafkaConfig[F[_]] {
  def setupAivenKafka(
    clientPrivateKey: ConfigResult[F, AivenKafkaClientPrivateKey],
    clientCertificate: ConfigResult[F, AivenKafkaClientCertificate],
    serviceCertificate: ConfigResult[F, AivenKafkaServiceCertificate]
  ): ConfigResult[F, AivenKafkaSetupDetails]
}
