package algae.ciris

import algae.Config
import ciris.{ConfigDecoder, ConfigValue}

trait CirisConfig[F[_]] extends Config[ConfigValue[F, ?], ConfigDecoder[String, ?]]
