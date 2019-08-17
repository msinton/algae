package algae.ciris

import _root_.cats.effect.Sync
import _root_.cats.syntax.flatMap._
import _root_.cats.syntax.functor._
import _root_.ciris._
import _root_.ciris.cats.effect._
import _root_.ciris.kubernetes._
import _root_.io.kubernetes.client.ApiClient

package object kubernetes {
  def createDefaultKubernetesConfig[F[_]](
    implicit F: Sync[F]
  ): F[KubernetesConfig[F]] =
    registerGcpAuth[F].flatMap { _ =>
      defaultApiClient[F].map(createKubernetesConfig[F])
    }

  def createKubernetesConfig[F[_]](apiClient: ApiClient)(
    implicit F: Sync[F]
  ): KubernetesConfig[F] =
    new KubernetesConfig[F] {
      def secret[A](namespace: String, name: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, SecretKey, String, A] =
        secretInNamespace(namespace, apiClient)
          .apply[A](name)

      def secret[A](namespace: String, name: String, key: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, SecretKey, String, A] =
        secretInNamespace(namespace, apiClient)
          .apply[A](name, key)

      def configMap[A](namespace: String, name: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, ConfigMapKey, String, A] =
        configMapInNamespace(namespace, apiClient)
          .apply[A](name)

      def configMap[A](namespace: String, name: String, key: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, ConfigMapKey, String, A] =
        configMapInNamespace(namespace, apiClient)
          .apply[A](name, key)

      override def env[A](key: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, String, String, A] = envF(key)

      override def prop[A](key: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigEntry[F, String, String, A] = propF(key)
    }
}
