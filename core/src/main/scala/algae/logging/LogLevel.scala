package algae.logging

import cats.Order
import cats.instances.int._

sealed abstract class LogLevel(val value: Int)

object LogLevel {
  case object Error extends LogLevel(3)
  case object Warn extends LogLevel(4)
  case object Info extends LogLevel(6)
  case object Debug extends LogLevel(7)

  implicit val logLevelOrder: Order[LogLevel] =
    Order.by(_.value)
}
