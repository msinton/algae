package algae.ciris.kubernetes

import algae.ciris.CirisConfig
import ciris.kubernetes.SecretInNamespace

trait KubernetesConfig[F[_]] extends CirisConfig[F] {
  def secretInNamespace(namespace: String): SecretInNamespace[F]
}
