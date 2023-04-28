object Command {
  sealed trait Command

  case class Add(amount: Int) extends Command

  case class Multiply(amount: Int) extends Command

  case class Divide(amount: Int) extends Command

}