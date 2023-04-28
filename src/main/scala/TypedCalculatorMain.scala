import Command._
import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, Props}
import akka.persistence.typed.PersistenceId

import scala.concurrent.ExecutionContextExecutor

object TypedCalculatorMain {

  trait CborSerialization

  val persistenceId: PersistenceId = PersistenceId.ofUniqueId("001")

  def apply(): Behavior[NotUsed] =
    Behaviors.setup {
      ctx =>
        val writeActorRef = ctx.spawn(TypedCalculatorWriteSide(), "Calc", Props.empty)
        writeActorRef ! Add(10)
        writeActorRef ! Multiply(2)
        writeActorRef ! Divide(5)

        Behaviors.same
    }

  def execute(command: Command): Behavior[NotUsed] =
    Behaviors.setup { ctx =>
      val writeActorRef = ctx.spawn(TypedCalculatorWriteSide(), "Calc", Props.empty)
      writeActorRef ! command

      Behaviors.same
    }

  def main(args: Array[String]): Unit = {
    val value = TypedCalculatorMain()
    implicit val system: ActorSystem[NotUsed] = ActorSystem(value, "akka_typed")

    TypedCalculatorReadSide(system)
    implicit val executionContext: ExecutionContextExecutor = system.executionContext
  }

}