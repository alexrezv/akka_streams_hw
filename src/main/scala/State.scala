import TypedCalculatorMain.CborSerialization

final case class State(value: Double) extends CborSerialization {
  def add(amount: Double): State = copy(value = value + amount)

  def multiply(amount: Double): State = copy(value = value * amount)

  def divide(amount: Double): State = copy(value = value / amount)

}

object State {
  val empty: State = State(0)
}
