object Event {
  sealed trait Event

  case class Added(id: Int, amount: Double) extends Event

  case class Multiplied(id: Int, amount: Double) extends Event

  case class Divided(id: Int, amount: Double) extends Event
}