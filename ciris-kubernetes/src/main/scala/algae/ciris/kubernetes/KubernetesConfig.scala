package algae.ciris.kubernetes

import algae.ciris.CirisConfig
import ciris.kubernetes.SecretKey
import ciris.{ConfigDecoder, ConfigEntry}

trait KubernetesConfig[F[_]] extends CirisConfig[F] {
  final def secret(namespace: String): KubernetesNamespace[F] = {
    val self = this
    new KubernetesNamespace[F] {
      override def apply[A](name: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, SecretKey, String, A] =
        self.secret(namespace, name)

      override def apply[A](name: String, key: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, SecretKey, String, A] =
        self.secret(namespace, name, key)

      override def toString: String =
        s"KubernetesNamespace($namespace)"
    }
  }

  def secret[A](namespace: String, name: String)(
    implicit decoder: ConfigDecoder[String, A]
  ): ConfigEntry[F, SecretKey, String, A]

  def secret[A](namespace: String, name: String, key: String)(
    implicit decoder: ConfigDecoder[String, A]
  ): ConfigEntry[F, SecretKey, String, A]
}
