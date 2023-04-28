import Command._
import Event._
import TypedCalculatorMain.persistenceId
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

object TypedCalculatorWriteSide {

  private def handleCommand(
                             persistenceId: String,
                             state: State,
                             command: Command,
                             ctx: ActorContext[Command]
                           ): Effect[Event, State] =
    command match {
      case Add(amount) =>
        ctx.log.info(s"receive adding for number: $amount and state is ${state.value}")
        persistEvent(ctx, Added(persistenceId.toInt, amount))
      case Multiply(amount) =>
        ctx.log.info(s"receive multiplying for number: $amount and state is ${state.value}")
        persistEvent(ctx, Multiplied(persistenceId.toInt, amount))
      case Divide(amount) =>
        ctx.log.info(s"receive dividing for number: $amount and state is ${state.value}")
        persistEvent(ctx, Divided(persistenceId.toInt, amount))
    }

  private def persistEvent(ctx: ActorContext[Command], event: Event): Effect[Event, State] = {
    Effect
      .persist(event)
      .thenRun {
        x => ctx.log.info(s"The state result is ${x.value}")
      }
  }

  private def handleEvent(state: State, event: Event, ctx: ActorContext[Command]): State =
    event match {
      case Added(_, amount) =>
        ctx.log.info(s"Handling event Added is: $amount and state is ${state.value}")
        state.add(amount)
      case Multiplied(_, amount) =>
        ctx.log.info(s"Handling event Multiplied is: $amount and state is ${state.value}")
        state.multiply(amount)
      case Divided(_, amount) =>
        ctx.log.info(s"Handling event Divided is: $amount and state is ${state.value}")
        state.divide(amount)
    }

  def apply(): Behavior[Command] =
    Behaviors.setup { ctx =>
      EventSourcedBehavior[Command, Event, State](
        persistenceId = persistenceId,
        State.empty,
        (state, command) => handleCommand(persistenceId.id, state, command, ctx),
        (state, event) => handleEvent(state, event, ctx)
      )
    }

}
