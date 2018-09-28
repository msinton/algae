package algae.kamon

import cats.effect.Sync
import cats.syntax.functor._
import kamon.Kamon
import kamon.influxdb.InfluxDBReporter
import kamon.util.Registration

package object influxdb {
  def withInfluxDbReporting[F[_], A](fa: F[A])(implicit F: Sync[F]): F[A] = {
    def register: F[Registration] =
      F.delay(Kamon.addReporter(new InfluxDBReporter()))

    def cancel(registration: Registration): F[Unit] =
      F.delay(registration.cancel()).void

    F.bracket(register)(_ => fa)(cancel)
  }
}
