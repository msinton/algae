package algae.ciris.kubernetes

import algae.ciris.CirisConfig
import ciris.kubernetes.{ConfigMapKey, SecretKey}
import ciris.{ConfigDecoder, ConfigEntry}

trait KubernetesConfig[F[_]] extends CirisConfig[F] {
  final def secret(namespace: String): KubernetesNamespace[F, SecretKey] = {
    val self = this
    new KubernetesNamespace[F, SecretKey] {
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

  final def configMap(namespace: String): KubernetesNamespace[F, ConfigMapKey] = {
    val self = this
    new KubernetesNamespace[F, ConfigMapKey] {
      override def apply[A](name: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, ConfigMapKey, String, A] =
        self.configMap(namespace, name)

      override def apply[A](name: String, key: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, ConfigMapKey, String, A] =
        self.configMap(namespace, name, key)

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

  def configMap[A](namespace: String, name: String)(
    implicit decoder: ConfigDecoder[String, A]
  ): ConfigEntry[F, ConfigMapKey, String, A]

  def configMap[A](namespace: String, name: String, key: String)(
    implicit decoder: ConfigDecoder[String, A]
  ): ConfigEntry[F, ConfigMapKey, String, A]
}
