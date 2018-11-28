package algae

trait KafkaProducer[F[_], M[_, _, _], R[_, _, _], K, V] {
  def produceBatched[P](message: M[K, V, P]): F[F[R[K, V, P]]]

  def produce[P](message: M[K, V, P]): F[R[K, V, P]]
}
