import algae.mtl.{DefaultMonadLog, MonadLog}
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.functor._
import cats.{Monad, Monoid}

package object algae {
  def createMonadLog[F[_], L](
    implicit
    F: Sync[F],
    L: Monoid[L]
  ): F[MonadLog[F, L]] =
    Ref[F].of(L.empty).map { ref =>
      new DefaultMonadLog[F, L] {
        override val monad: Monad[F] = F
        override val monoid: Monoid[L] = L
        override def get: F[L] = ref.get
        override def set(l: L): F[Unit] = ref.set(l)
      }
    }
}
