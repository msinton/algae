package algae.mtl

import cats.Monoid
import cats.mtl.{DefaultMonadState, MonadState}

trait MonadLog[F[_], L] extends MonadState[F, L] with Serializable {
  val monoid: Monoid[L]

  def log(l: L): F[Unit]

  def clear: F[Unit]

  def flush(f: L => F[Unit]): F[Unit]
}

object MonadLog {
  def apply[F[_], L](implicit log: MonadLog[F, L]): MonadLog[F, L] =
    log

  def log[F[_], L](l: L)(implicit log: MonadLog[F, L]): F[Unit] =
    log.log(l)

  def clear[F[_], L](implicit log: MonadLog[F, L]): F[Unit] =
    log.clear

  def flush[F[_], L](f: L => F[Unit])(implicit log: MonadLog[F, L]): F[Unit] =
    log.flush(f)
}

trait DefaultMonadLog[F[_], L] extends DefaultMonadState[F, L] with MonadLog[F, L] {
  def log(l: L): F[Unit] =
    modify(monoid.combine(_, l))

  def clear: F[Unit] =
    set(monoid.empty)

  def flush(f: L => F[Unit]): F[Unit] =
    monad.flatMap(monad.flatMap(get)(f))(_ => clear)
}
