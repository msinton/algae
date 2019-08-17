package algae.ciris.kubernetes

import ciris.kubernetes.KubernetesKey
import ciris.{ConfigDecoder, ConfigEntry}

abstract class KubernetesNamespace[F[_], K <: KubernetesKey] private[kubernetes] {
  def apply[A](name: String)(
    implicit decoder: ConfigDecoder[String, A]
  ): ConfigEntry[F, K, String, A]

  def apply[A](name: String, key: String)(
    implicit decoder: ConfigDecoder[String, A]
  ): ConfigEntry[F, K, String, A]
}
