package algae.ciris.kubernetes

import ciris.kubernetes.SecretKey
import ciris.{ConfigDecoder, ConfigEntry}

abstract class KubernetesNamespace[F[_]] private[kubernetes] {
  def apply[A](name: String)(
    implicit decoder: ConfigDecoder[String, A]
  ): ConfigEntry[F, SecretKey, String, A]

  def apply[A](name: String, key: String)(
    implicit decoder: ConfigDecoder[String, A]
  ): ConfigEntry[F, SecretKey, String, A]
}
