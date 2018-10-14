package algae

import java.lang.StringBuilder

import algae.syntax.logging._
import cats.data.Chain
import cats.syntax.foldable._
import cats.{Always, Eval, Foldable, Order}

package object logging {
  def logMessage[G[_], E](ge: G[E])(
    implicit
    G: Foldable[G],
    E: LogEntry[E]
  ): Option[Eval[String]] = {
    if (ge.isEmpty) None
    else
      Some(Always {
        var entries = 0
        var messageLength = 0
        var messages = Chain.empty[String]

        ge.foldLeft(()) { (_, e) =>
          entries += 1
          val message = e.message
          messageLength += message.length
          messages = messages.append(message)
        }

        if (entries == 1)
          messages.foldLeft("")((_, m) => m)
        else {
          var first = true
          messages
            .foldLeft(new StringBuilder(messageLength + 3 * entries - 1)) { (b, m) =>
              if (first) first = false
              else b.append('\n')

              b.append("- ").append(m)
            }
            .toString
        }
      })
  }

  def logLevel[G[_], E](ge: G[E])(
    implicit
    G: Foldable[G],
    E: LogEntry[E]
  ): Option[LogLevel] =
    ge.minimumOption(Order.by(_.level)).map(_.level)
}
