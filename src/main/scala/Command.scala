object Command {
  sealed trait Command

  case class Add(amount: Double) extends Command

  case class Multiply(amount: Double) extends Command

  case class Divide(amount: Double) extends Command

}