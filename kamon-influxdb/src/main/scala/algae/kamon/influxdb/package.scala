package algae.kamon

import cats.effect.{Resource, Sync}
import cats.syntax.functor._
import kamon.Kamon
import kamon.influxdb.InfluxDBReporter
import kamon.util.Registration

package object influxdb {
  def influxDbRegistration[F[_]](implicit F: Sync[F]): Resource[F, Registration] = {
    val acquire = F.delay(Kamon.addReporter(new InfluxDBReporter()))

    def release(registration: Registration): F[Unit] =
      F.delay(registration.cancel()).void

    Resource.make(acquire)(release)
  }
}
