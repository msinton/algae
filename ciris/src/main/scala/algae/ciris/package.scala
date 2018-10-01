package algae

import _root_.cats.effect.Sync
import _root_.ciris._
import _root_.ciris.cats.effect._

package object ciris {
  def createCirisConfig[F[_]](implicit F: Sync[F]): CirisConfig[F] =
    new CirisConfig[F] {
      override def env[A](key: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigValue[F, A] = envF(key)

      override def prop[A](key: String)(
        implicit decoder: ConfigDecoder[String, A]
      ): ConfigValue[F, A] = propF(key)
    }
}
