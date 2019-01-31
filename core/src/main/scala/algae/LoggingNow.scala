package algae

import algae.logging.{LogEntry, LogLevel, logLevel, logMessage}
import cats.{Applicative, Eval, Foldable, MonoidK}
import cats.instances.option._
import cats.syntax.apply._
import cats.syntax.option._
import cats.syntax.semigroupk._

trait LoggingNow[F[_], G[_], E, M] {
  def logNowN(ge: G[E]): F[Unit]

  def logNow(e: E, es: E*): F[Unit]

  def logNowContextN(gm: G[M])(ge: G[E]): F[Unit]

  def logNowContext(gm: G[M])(e: E, es: E*): F[Unit]
}

object LoggingNow {
  def create[F[_], G[_], E, M](
    dispatch: (G[E], G[M]) => F[Unit]
  )(
    implicit
    A: Applicative[G],
    K: MonoidK[G]
  ): LoggingNow[F, G, E, M] =
    new LoggingNow[F, G, E, M] {
      private[this] def g[A](a: A, as: A*): G[A] =
        as.foldLeft(A.pure(a))(_ combineK A.pure(_))

      override def logNowN(ge: G[E]): F[Unit] =
        logNowContextN(K.empty)(ge)

      override def logNow(e: E, es: E*): F[Unit] =
        logNowN(g(e, es: _*))

      override def logNowContextN(gm: G[M])(ge: G[E]): F[Unit] =
        dispatch(ge, gm)

      override def logNowContext(gm: G[M])(e: E, es: E*): F[Unit] =
        logNowContextN(gm)(g(e, es: _*))
    }

  def createDefault[F[_], G[_], E, M](
    dispatch: (Eval[String], G[M], LogLevel) => F[Unit]
  )(
    implicit
    F: Applicative[F],
    A: Applicative[G],
    G: Foldable[G],
    K: MonoidK[G],
    E: LogEntry[E]
  ): LoggingNow[F, G, E, M] =
    create {
      case (ge, gm) =>
        (logMessage(ge), gm.some, logLevel(ge))
          .mapN(dispatch)
          .getOrElse(F.unit)
    }
}
