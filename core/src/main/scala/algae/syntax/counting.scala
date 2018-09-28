package algae.syntax
import algae.counting.CounterIncrement

object counting {
  implicit final class CounterIncrementOps[A](private val a: A) extends AnyVal {
    def counterName(implicit increment: CounterIncrement[A]): String = increment.counterName(a)
    def tags(implicit increment: CounterIncrement[A]): Map[String, String] = increment.tags(a)
    def times(implicit increment: CounterIncrement[A]): Long = increment.times(a)
  }
}
