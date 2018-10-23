package algae

import algae.logging._
import algae.mtl.MonadLog
import cats.instances.option._
import cats.syntax.apply._
import cats.syntax.option._
import cats.syntax.semigroupk._
import cats.{Applicative, Eval, Foldable, Monad, MonoidK}

trait Logging[F[_], G[_], E, M] extends LoggingNow[F, G, E, M] {
  def logN(ge: G[E]): F[Unit]

  def log(e: E, es: E*): F[Unit]

  def clearLogs: F[Unit]

  def dispatchLogs: F[Unit]

  def dispatchLogsContext(gm: G[M]): F[Unit]

  def extractLogs: F[G[E]]
}

object Logging {
  def create[F[_], G[_], E, M](
    monadLog: MonadLog[F, G[E]],
    dispatch: (Eval[String], G[M], LogLevel) => F[Unit]
  )(
    implicit
    A: Applicative[G],
    G: Foldable[G],
    K: MonoidK[G],
    E: LogEntry[E]
  ): Logging[F, G, E, M] =
    new Logging[F, G, E, M] {
      private val F: Monad[F] = monadLog.monad

      private def g[A](a: A, as: A*): G[A] =
        as.foldLeft(A.pure(a))(_ combineK A.pure(_))

      private def dispatchNow(ge: G[E], gm: G[M]): F[Unit] =
        (logMessage(ge), gm.some, logLevel(ge))
          .mapN(dispatch)
          .getOrElse(F.unit)

      override def logN(ge: G[E]): F[Unit] =
        monadLog.log(ge)

      override def log(e: E, es: E*): F[Unit] =
        monadLog.log(g(e, es: _*))

      override def logNowN(ge: G[E]): F[Unit] =
        dispatchNow(ge, K.empty)

      override def logNow(e: E, es: E*): F[Unit] =
        dispatchNow(g(e, es: _*), K.empty)

      override def logNowContextN(gm: G[M])(ge: G[E]): F[Unit] =
        dispatchNow(ge, gm)

      override def logNowContext(gm: G[M])(e: E, es: E*): F[Unit] =
        dispatchNow(g(e, es: _*), gm)

      override def clearLogs: F[Unit] =
        monadLog.clear

      override def dispatchLogs: F[Unit] =
        monadLog.flush(dispatchNow(_, K.empty))

      override def dispatchLogsContext(gm: G[M]): F[Unit] =
        monadLog.flush(dispatchNow(_, gm))

      override def extractLogs: F[G[E]] =
        monadLog.get
    }
}
