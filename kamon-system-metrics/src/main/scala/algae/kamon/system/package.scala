package algae.kamon

import cats.effect.Sync
import kamon.system.SystemMetrics

package object system {
  def withSystemMetricsCollection[F[_], A](fa: F[A])(implicit F: Sync[F]): F[A] = {
    def start: F[Unit] =
      F.delay(SystemMetrics.startCollecting())

    def stop: F[Unit] =
      F.delay(SystemMetrics.stopCollecting())

    F.bracket(start)(_ => fa)(_ => stop)
  }
}
