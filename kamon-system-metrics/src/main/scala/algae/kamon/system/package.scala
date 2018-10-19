package algae.kamon

import cats.effect.{Resource, Sync}
import kamon.system.SystemMetrics

package object system {
  def systemMetricsCollection[F[_]](implicit F: Sync[F]): Resource[F, Unit] = {
    val acquire = F.delay(SystemMetrics.startCollecting())
    val release = F.delay(SystemMetrics.stopCollecting())
    Resource.make(acquire)(_ => release)
  }
}
