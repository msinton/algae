package algae

import algae.syntax.logging._
import cats.instances.string._
import cats.syntax.foldable._
import cats.syntax.option._
import cats.syntax.semigroup._
import cats.{Always, Eval, Foldable, Order}

package object logging {
  def logMessage[G[_], E](ge: G[E])(
    implicit
    G: Foldable[G],
    E: LogEntry[E]
  ): Option[Eval[String]] = {
    def entry(e: E): String =
      "- " combine e.message

    def append(s: String, e: E): String =
      s combine "\n" combine entry(e)

    def combine(a: E, b: E): String =
      append(entry(a), b)

    ge.foldLeft[Option[(Option[E], Eval[String])]](None) {
        case (None, a)               => (a.some -> Always(a.message)).some
        case (Some((Some(a), _)), b) => (none -> Always(combine(a, b))).some
        case (Some((None, a)), b)    => (none -> a.map(append(_, b))).some
      }
      .map { case (_, message) => message }
  }

  def logLevel[G[_], E](ge: G[E])(
    implicit
    G: Foldable[G],
    E: LogEntry[E]
  ): Option[LogLevel] =
    ge.minimumOption(Order.by(_.level)).map(_.level)
}
