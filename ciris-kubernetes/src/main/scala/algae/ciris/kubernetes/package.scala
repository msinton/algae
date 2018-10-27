package algae.ciris

import _root_.cats.effect.{Concurrent, Sync}
import _root_.cats.syntax.flatMap._
import _root_.cats.syntax.functor._
import _root_.ciris._
import _root_.ciris.cats.effect._
import _root_.ciris.kubernetes._
import _root_.io.kubernetes.client.ApiClient

package object kubernetes {
  def createDefaultKubernetesConfig[F[_]](
    implicit F: Concurrent[F]
  ): F[KubernetesConfig[F]] =
    defaultApiClient[F].flatMap { apiClient =>
      registerGcpAuthenticator[F].map { registerAuth =>
        createKubernetesConfig(apiClient, registerAuth)
      }
    }

  def createKubernetesConfig[F[_]](
    apiClient: F[ApiClient],
    registerAuth: F[Unit]
  )(implicit F: Sync[F]): KubernetesConfig[F] =
    new KubernetesConfig[F] {
      def secret[A](namespace: String, name: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, SecretKey, String, A] =
        secretInNamespace(namespace, apiClient, registerAuth)
          .apply[A](name)

      def secret[A](namespace: String, name: String, key: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, SecretKey, String, A] =
        secretInNamespace(namespace, apiClient, registerAuth)
          .apply[A](name, key)

      override def env[A](key: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, String, String, A] = envF(key)

      override def prop[A](key: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, String, String, A] = propF(key)
    }
}
