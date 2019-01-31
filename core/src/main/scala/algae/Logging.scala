package algae

import algae.logging._
import algae.mtl.MonadLog
import cats.{Applicative, Eval, Foldable, MonoidK}
import cats.instances.option._
import cats.syntax.apply._
import cats.syntax.option._
import cats.syntax.semigroupk._

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
    dispatch: (G[E], G[M]) => F[Unit]
  )(
    implicit
    A: Applicative[G],
    K: MonoidK[G]
  ): Logging[F, G, E, M] =
    new Logging[F, G, E, M] {
      private def g[A](a: A, as: A*): G[A] =
        as.foldLeft(A.pure(a))(_ combineK A.pure(_))

      override def logN(ge: G[E]): F[Unit] =
        monadLog.log(ge)

      override def log(e: E, es: E*): F[Unit] =
        monadLog.log(g(e, es: _*))

      override def logNowN(ge: G[E]): F[Unit] =
        logNowContextN(K.empty)(ge)

      override def logNow(e: E, es: E*): F[Unit] =
        logNowContext(K.empty)(e, es: _*)

      override def logNowContextN(gm: G[M])(ge: G[E]): F[Unit] =
        dispatch(ge, gm)

      override def logNowContext(gm: G[M])(e: E, es: E*): F[Unit] =
        logNowContextN(gm)(g(e, es: _*))

      override def clearLogs: F[Unit] =
        monadLog.clear

      override def dispatchLogs: F[Unit] =
        monadLog.flush(logNowN)

      override def dispatchLogsContext(gm: G[M]): F[Unit] =
        monadLog.flush(logNowContextN(gm))

      override def extractLogs: F[G[E]] =
        monadLog.get
    }

  def createDefault[F[_], G[_], E, M](
    monadLog: MonadLog[F, G[E]],
    dispatch: (Eval[String], G[M], LogLevel) => F[Unit]
  )(
    implicit
    A: Applicative[G],
    G: Foldable[G],
    K: MonoidK[G],
    E: LogEntry[E]
  ): Logging[F, G, E, M] =
    create(monadLog, {
      case (ge, gm) =>
        (logMessage(ge), gm.some, logLevel(ge))
          .mapN(dispatch)
          .getOrElse(monadLog.monad.unit)
    })
}
