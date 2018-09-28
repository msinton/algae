package algae

import algae.logging.{LogEntry, LogLevel, MdcEntry}
import algae.mtl.MonadLog
import algae.syntax.logging._
import cats.effect.Sync
import cats.syntax.apply._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.{Applicative, Foldable, MonoidK}
import org.slf4j.{LoggerFactory, MDC}

package object slf4j {
  def createLogging[F[_], G[_], E, M](
    name: String,
    monadLog: MonadLog[F, G[E]]
  )(
    implicit
    F: Sync[F],
    A: Applicative[G],
    G: Foldable[G],
    K: MonoidK[G],
    E: LogEntry[E],
    M: MdcEntry[M]
  ): F[Logging[F, G, E, M]] =
    F.delay(LoggerFactory.getLogger(name)).map { logger =>
      Logging.create[F, G, E, M](monadLog, {
        case (message, mdc, level) =>
          val levelEnabled = level match {
            case LogLevel.Error => logger.isErrorEnabled
            case LogLevel.Warn  => logger.isWarnEnabled
            case LogLevel.Info  => logger.isInfoEnabled
            case LogLevel.Debug => logger.isDebugEnabled
          }

          if (levelEnabled) {
            val setContext =
              mdc.foldLeft(F.unit) { (ms, m) =>
                ms *> F.delay(MDC.put(m.key, m.value))
              }

            val resetContext =
              mdc.foldLeft(F.unit) { (ms, m) =>
                ms *> F.delay(MDC.remove(m.key)).void
              }

            F.bracket(setContext) { _ =>
              F.delay(level match {
                case LogLevel.Error => logger.error(message.value)
                case LogLevel.Warn  => logger.warn(message.value)
                case LogLevel.Info  => logger.info(message.value)
                case LogLevel.Debug => logger.debug(message.value)
              })
            }(_ => resetContext)
          } else F.unit
      })
    }
}
