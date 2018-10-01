package algae.ciris

import _root_.cats.effect.{Concurrent, Sync}
import _root_.cats.syntax.flatMap._
import _root_.cats.syntax.functor._
import _root_.ciris._
import _root_.ciris.cats.effect._
import _root_.ciris.kubernetes.{secretInNamespace => secret, _}
import _root_.io.kubernetes.client.ApiClient

package object kubernetes {
  def createDefaultKubernetesConfig[F[_]](
    implicit F: Concurrent[F]
  ): F[KubernetesConfig[F]] =
    for {
      apiClient <- defaultApiClient[F]
      registerAuth <- registerGcpAuthenticator[F]
    } yield createKubernetesConfig(apiClient, registerAuth)

  def createKubernetesConfig[F[_]](
    apiClient: F[ApiClient],
    registerAuth: F[Unit]
  )(implicit F: Sync[F]): KubernetesConfig[F] =
    new KubernetesConfig[F] {
      override def secretInNamespace(namespace: String): SecretInNamespace[F] =
        secret(namespace, apiClient, registerAuth)

      override def env[A](key: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigValue[F, A] = envF(key)

      override def prop[A](key: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigValue[F, A] = propF(key)
    }
}
