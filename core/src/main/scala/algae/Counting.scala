package algae
import algae.mtl.MonadLog
import cats.Applicative
import cats.kernel.Monoid
import cats.syntax.semigroup._

trait Counting[F[_], G[_], E] {
  def countN(ge: G[E]): F[Unit]

  def count(e: E, es: E*): F[Unit]

  def countNowN(ge: G[E]): F[Unit]

  def countNow(e: E, es: E*): F[Unit]

  def clearCounts: F[Unit]

  def dispatchCounts: F[Unit]

  def extractCounts: F[G[E]]
}

object Counting {
  def create[F[_], G[_], E](
    monadLog: MonadLog[F, G[E]],
    dispatch: G[E] => F[Unit]
  )(implicit G: Applicative[G]): Counting[F, G, E] =
    new Counting[F, G, E] {
      private implicit val monoid: Monoid[G[E]] = monadLog.monoid

      private def g(e: E, es: E*): G[E] =
        es.foldLeft(G.pure(e))(_ combine G.pure(_))

      override def countN(ge: G[E]): F[Unit] =
        monadLog.log(ge)

      override def count(e: E, es: E*): F[Unit] =
        monadLog.log(g(e, es: _*))

      override def countNowN(ge: G[E]): F[Unit] =
        dispatch(ge)

      override def countNow(e: E, es: E*): F[Unit] =
        dispatch(g(e, es: _*))

      override def clearCounts: F[Unit] =
        monadLog.clear

      override def dispatchCounts: F[Unit] =
        monadLog.flush(dispatch)

      override def extractCounts: F[G[E]] =
        monadLog.get
    }
}
