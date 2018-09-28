package algae.logging

trait MdcEntry[A] {
  def key(a: A): String

  def value(a: A): String
}

object MdcEntry {
  def apply[A](implicit mdc: MdcEntry[A]): MdcEntry[A] = mdc

  def key[A](a: A)(implicit mdc: MdcEntry[A]): String = mdc.key(a)

  def value[A](a: A)(implicit mdc: MdcEntry[A]): String = mdc.value(a)

  def from[A](key: A => String, value: A => String): MdcEntry[A] = {
    val (_key, _value) = (key, value)
    new MdcEntry[A] {
      override def key(a: A): String = _key(a)
      override def value(a: A): String = _value(a)
    }
  }
}
