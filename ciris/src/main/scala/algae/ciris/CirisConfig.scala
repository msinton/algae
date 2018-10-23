package algae.ciris

import algae.Config
import ciris.{ConfigDecoder, ConfigEntry}

trait CirisConfig[F[_]] extends Config[ConfigEntry[F, String, String, ?], ConfigDecoder[String, ?]]
