package algae

import algae.mtl.MonadLog
import algae.mtl.laws.discipline.MonadLogTests
import cats.data.Chain
import cats.effect.IO
import cats.effect.laws.discipline.arbitrary._
import cats.implicits._
import cats.laws.discipline.arbitrary._

final class MonadLogSpec extends BaseSuite {
  checkAllAsync("MonadLog[IO, Chain[Int]]") { implicit testContext =>
    implicit val monadLog: MonadLog[IO, Chain[Int]] =
      createMonadLog[IO, Chain[Int]].unsafeRunSync

    MonadLogTests[IO, Chain[Int]].monadLog[Int]
  }
}
