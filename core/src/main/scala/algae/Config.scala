package algae

trait Config[F[_], G[_]] {
  def env[A](key: String)(implicit decoder: G[A]): F[A]

  def prop[A](key: String)(implicit decoder: G[A]): F[A]
}
