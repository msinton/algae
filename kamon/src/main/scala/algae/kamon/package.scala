package algae

import _root_.kamon.Kamon
import algae.counting.CounterIncrement
import algae.mtl.MonadLog
import algae.syntax.counting._
import cats.effect.Sync
import cats.syntax.apply._
import cats.syntax.foldable._
import cats.{Applicative, Foldable}

package object kamon {
  def createCounting[F[_], G[_], E](monadLog: MonadLog[F, G[E]])(
    implicit
    F: Sync[F],
    A: Applicative[G],
    G: Foldable[G],
    E: CounterIncrement[E]
  ): Counting[F, G, E] = {
    def count(e: E): F[Unit] =
      F.delay {
        Kamon
          .counter(e.counterName)
          .refine(e.tags)
          .increment(e.times)
      }

    Counting.create[F, G, E](monadLog, _.foldLeft(F.unit)(_ *> count(_)))
  }
}
