object Event {
  sealed trait Event

  case class Added(id: Int, amount: Int) extends Event

  case class Multiplied(id: Int, amount: Int) extends Event

  case class Divided(id: Int, amount: Int) extends Event
}