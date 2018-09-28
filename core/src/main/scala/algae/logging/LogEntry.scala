package algae.logging

trait LogEntry[A] {
  def level(a: A): LogLevel

  def message(a: A): String
}

object LogEntry {
  def apply[A](implicit log: LogEntry[A]): LogEntry[A] = log

  def level[A](a: A)(implicit log: LogEntry[A]): LogLevel = log.level(a)

  def message[A](a: A)(implicit log: LogEntry[A]): String = log.message(a)

  def from[A](level: A => LogLevel, message: A => String): LogEntry[A] = {
    val (_level, _message) = (level, message)
    new LogEntry[A] {
      override def level(a: A): LogLevel = _level(a)
      override def message(a: A): String = _message(a)
    }
  }
}
