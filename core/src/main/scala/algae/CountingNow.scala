package algae

import cats.syntax.monoid._
import cats.{Applicative, Monoid}

trait CountingNow[F[_], G[_], E] {
  def countNowN(ge: G[E]): F[Unit]

  def countNow(e: E, es: E*): F[Unit]
}

object CountingNow {
  def create[F[_], G[_], E](dispatch: G[E] => F[Unit])(
    implicit G: Applicative[G],
    M: Monoid[G[E]]
  ): CountingNow[F, G, E] =
    new CountingNow[F, G, E] {
      private def g(e: E, es: E*): G[E] =
        es.foldLeft(G.pure(e))(_ combine G.pure(_))

      override def countNowN(ge: G[E]): F[Unit] =
        dispatch(ge)

      override def countNow(e: E, es: E*): F[Unit] =
        dispatch(g(e, es: _*))
    }
}
