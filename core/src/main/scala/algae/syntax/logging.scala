package algae.syntax

import algae.logging.{LogEntry, LogLevel, MdcEntry}

object logging {
  implicit final class LogEntryOps[A](private val a: A) extends AnyVal {
    def level(implicit log: LogEntry[A]): LogLevel = log.level(a)
    def message(implicit log: LogEntry[A]): String = log.message(a)
  }

  implicit final class MdcEntryOps[A](private val a: A) extends AnyVal {
    def key(implicit mdc: MdcEntry[A]): String = mdc.key(a)
    def value(implicit mdc: MdcEntry[A]): String = mdc.value(a)
  }
}
