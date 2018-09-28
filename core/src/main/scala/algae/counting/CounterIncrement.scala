package algae.counting

trait CounterIncrement[A] {
  def counterName(a: A): String
  def tags(a: A): Map[String, String]
  def times(a: A): Long
}

object CounterIncrement {
  def apply[A](implicit increment: CounterIncrement[A]): CounterIncrement[A] =
    increment

  def counterName[A](a: A)(implicit increment: CounterIncrement[A]): String =
    increment.counterName(a)

  def tags[A](a: A)(implicit increment: CounterIncrement[A]): Map[String, String] =
    increment.tags(a)

  def times[A](a: A)(implicit increment: CounterIncrement[A]): Long =
    increment.times(a)

  def from[A](
    counterName: A => String,
    tags: A => Map[String, String],
    times: A => Long
  ): CounterIncrement[A] = {
    val (_counterName, _tags, _times) = (counterName, tags, times)
    new CounterIncrement[A] {
      override def counterName(a: A): String = _counterName(a)
      override def tags(a: A): Map[String, String] = _tags(a)
      override def times(a: A): Long = _times(a)
    }
  }
}
