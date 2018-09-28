package algae.mtl.laws

import algae.mtl.MonadLog
import cats.Monoid
import cats.laws.IsEqArrow
import cats.mtl.MonadState
import cats.mtl.laws.MonadStateLaws
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.semigroup._

trait MonadLogLaws[F[_], L] extends MonadStateLaws[F, L] {
  implicit val logInstance: MonadLog[F, L]
  implicit val monoid: Monoid[L] = logInstance.monoid

  import logInstance._
  import monad.{pure, unit}
  import monoid.empty

  def logThenClearDoesNothing(l: L) =
    log(l) *> clear <-> unit

  def logThenGetReturnsLog(l: L) =
    log(l) *> get.flatMap(clear.as) <-> log(l) *> clear.as(l)

  def logsThenGetReturnsCombinedLog(l1: L, l2: L) =
    log(l1) *> log(l2) *> get.flatMap(clear.as) <->
      log(l1) *> log(l2) *> clear.as(l1 combine l2)

  def clearThenGetReturnsEmpty =
    clear *> get <-> pure(empty)

  def clearThenClearClearsOnce =
    clear *> clear <-> clear

  def flushIsGetThenClear(f: L => F[Unit]) =
    flush(f) <-> get.flatMap(f) *> clear
}

object MonadLogLaws {
  def apply[F[_], L](implicit instance0: MonadLog[F, L]): MonadLogLaws[F, L] =
    new MonadLogLaws[F, L] {
      override lazy val stateInstance: MonadState[F, L] = instance0
      override lazy val logInstance: MonadLog[F, L] = instance0
    }
}
