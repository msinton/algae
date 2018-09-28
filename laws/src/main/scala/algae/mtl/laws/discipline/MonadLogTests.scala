package algae.mtl.laws.discipline

import algae.mtl.MonadLog
import algae.mtl.laws.MonadLogLaws
import cats.Eq
import cats.laws.discipline._
import cats.mtl.MonadState
import cats.mtl.laws.discipline.MonadStateTests
import org.scalacheck.Prop.forAll
import org.scalacheck._

trait MonadLogTests[F[_], L] extends MonadStateTests[F, L] {
  implicit val logInstance: MonadLog[F, L]
  override def laws: MonadLogLaws[F, L] = MonadLogLaws[F, L]

  def monadLog[A](
    implicit
    ArbA: Arbitrary[A],
    ArbFA: Arbitrary[F[A]],
    ArbFU: Arbitrary[F[Unit]],
    ArbL: Arbitrary[L],
    CogenL: Cogen[L],
    EqFL: Eq[F[L]],
    EqFU: Eq[F[Unit]]
  ): RuleSet = {
    new DefaultRuleSet(
      name = "monadLog",
      parent = Some(monadState[A]),
      "log then clear does nothing" -> forAll(laws.logThenClearDoesNothing _),
      "log then get returns log" -> forAll(laws.logThenGetReturnsLog _),
      "logs then get returns combined log" -> forAll(laws.logsThenGetReturnsCombinedLog _),
      "clear then get returns empty" -> laws.clearThenGetReturnsEmpty,
      "clear then clear clears once" -> laws.clearThenClearClearsOnce,
      "flush is get then clear" -> forAll(laws.flushIsGetThenClear _)
    )
  }
}

object MonadLogTests {
  def apply[F[_], L](implicit instance0: MonadLog[F, L]): MonadLogTests[F, L] =
    new MonadLogTests[F, L] {
      override val logInstance: MonadLog[F, L] = instance0
      override implicit val stateInstance: MonadState[F, L] = instance0
    }
}
