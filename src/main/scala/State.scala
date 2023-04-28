import TypedCalculatorMain.CborSerialization

final case class State(value: Int) extends CborSerialization {
  def add(amount: Int): State = copy(value = value + amount)

  def multiply(amount: Int): State = copy(value = value * amount)

  def divide(amount: Int): State = copy(value = value / amount)

}

object State {
  val empty: State = State(0)
}
