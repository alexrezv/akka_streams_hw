import Command._
import akka.NotUsed
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, Props}
import akka.persistence.typed.PersistenceId
import akka.stream.alpakka.slick.scaladsl.SlickSession

import scala.concurrent.ExecutionContextExecutor

object TypedCalculatorMain {

  trait CborSerialization

  val persistenceId: PersistenceId = PersistenceId.ofUniqueId("001")

  def apply(): Behavior[NotUsed] =
    Behaviors.setup {
      ctx =>
        val writeActorRef = ctx.spawn(TypedCalculatorWriteSide(), "Calc", Props.empty)
        writeActorRef ! Add(15)
        writeActorRef ! Divide(7)
        writeActorRef ! Multiply(3)

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

    implicit val session: SlickSession = SlickSession.forConfig("slick-postgres")

    TypedCalculatorReadSide(system)
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    system.whenTerminated.onComplete(_ => session.close())
  }

}