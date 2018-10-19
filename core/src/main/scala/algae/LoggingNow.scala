package algae

import algae.logging.{LogEntry, LogLevel, logLevel, logMessage}
import cats.instances.option._
import cats.syntax.apply._
import cats.syntax.option._
import cats.syntax.semigroupk._
import cats.{Applicative, Eval, Foldable, MonoidK}

trait LoggingNow[F[_], G[_], E, M] {
  def logNowN(ge: G[E]): F[Unit]

  def logNow(e: E, es: E*): F[Unit]

  def logNowContextN(gm: G[M])(ge: G[E]): F[Unit]

  def logNowContext(gm: G[M])(e: E, es: E*): F[Unit]
}

object LoggingNow {
  def create[F[_], G[_], E, M](
    dispatch: (Eval[String], G[M], LogLevel) => F[Unit]
  )(
    implicit
    F: Applicative[F],
    A: Applicative[G],
    G: Foldable[G],
    K: MonoidK[G],
    E: LogEntry[E]
  ): LoggingNow[F, G, E, M] =
    new LoggingNow[F, G, E, M] {
      private def g[A](a: A, as: A*): G[A] =
        as.foldLeft(A.pure(a))(_ combineK A.pure(_))

      private def dispatchNow(ge: G[E], gm: G[M]): F[Unit] =
        (logMessage(ge), gm.some, logLevel(ge))
          .mapN(dispatch)
          .getOrElse(F.unit)

      override def logNowN(ge: G[E]): F[Unit] =
        dispatchNow(ge, K.empty)

      override def logNow(e: E, es: E*): F[Unit] =
        dispatchNow(g(e, es: _*), K.empty)

      override def logNowContextN(gm: G[M])(ge: G[E]): F[Unit] =
        dispatchNow(ge, gm)

      override def logNowContext(gm: G[M])(e: E, es: E*): F[Unit] =
        dispatchNow(g(e, es: _*), gm)
    }

  implicit def fromLogging[F[_], G[_], E, M](
    implicit logging: Logging[F, G, E, M]
  ): LoggingNow[F, G, E, M] =
    new LoggingNow[F, G, E, M] {
      override def logNowN(ge: G[E]): F[Unit] =
        logging.logNowN(ge)

      override def logNow(e: E, es: E*): F[Unit] =
        logging.logNow(e, es: _*)

      override def logNowContextN(gm: G[M])(ge: G[E]): F[Unit] =
        logging.logNowContextN(gm)(ge)

      override def logNowContext(gm: G[M])(e: E, es: E*): F[Unit] =
        logging.logNowContext(gm)(e, es: _*)
    }
}
