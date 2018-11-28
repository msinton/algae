package algae

trait KafkaConsumer[S[_[_], _], M[_[_], _, _], F[_], G[_], T, K, V] {
  def stream: S[F, M[F, K, V]]

  def partitionedStream: S[F, S[F, M[F, K, V]]]

  def subscribe(topics: G[T]): S[F, Unit]
}
