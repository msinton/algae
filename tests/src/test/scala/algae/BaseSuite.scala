package algae

import cats.effect.laws.util.{TestContext, TestInstances}
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers
import org.typelevel.discipline.Laws

abstract class BaseSuite extends FunSuite with Checkers with TestInstances {
  def checkAllAsync(name: String)(f: TestContext => Laws#RuleSet): Unit = {
    val ruleSet = f(TestContext())
    for ((id, prop) ← ruleSet.all.properties)
      test(name + "." + id)(check(prop))
  }
}
