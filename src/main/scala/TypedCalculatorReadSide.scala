import CalculatorRepository.{getLatestOffsetAndResult, initDatabase, updatedResultAndOffset}
import Event._
import TypedCalculatorMain.persistenceId
import akka.actor.typed.ActorSystem
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ClosedShape, FlowShape, SinkShape, SourceShape}
import akka.{NotUsed, actor}

case class TypedCalculatorReadSide(system: ActorSystem[NotUsed]) {
  initDatabase()

  implicit val materializer: actor.ActorSystem = system.classicSystem
  var (offset, latestCalculatedResult) = getLatestOffsetAndResult
  private val startOffset: Int = if (offset == 1) 1 else offset + 1

  private val readJournal = PersistenceQuery(system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  /*
  /**
   * В read side приложения с архитектурой CQRS (объект TypedCalculatorReadSide) необходимо разделить бизнес логику и запись в целевой получатель, т.е.
   * 1) Persistence Query должно находиться в Source
   * 2) Обновление состояния необходимо переместить в отдельный от записи в БД флоу
   * 3) ! Задание со звездочкой: вместо CalculatorRepository создать Sink c любой БД (например Postgres из docker-compose файла).
   * Для последнего задания пригодится документация - https://doc.akka.io/docs/alpakka/current/slick.html#using-a-slick-flow-or-sink
   * Результат выполненного д.з. необходимо оформить либо на github gist либо PR к текущему репозиторию.
   *
   * */

  как делать:
  1. в типах int заменить на double
  2. изменения в строках 125-148
  3. добавить функцию updateState в которой будет паттерн матчинг событий Added Multiplied Divided
  4. создаете graphDsl  в котором: builder.add(source)
  5. builder.add(Flow[EventEnvelope].map( e => updateState(e.event, e.seqNr)))
   */


  private val source: Source[EventEnvelope, NotUsed] = readJournal.eventsByPersistenceId(persistenceId.id, startOffset, Long.MaxValue)

  /*
    // homework, spoiler
      def updateState(event: Any, seqNum: Long): Result ={
        val newState = event match {
          case Added(_amount)=>
            ???
          case Multiplied(_,amount)=>
            ???
          case Divided(_amount)=>
            ???
        }
        Result(newState, seqNum)
      }

      val graph = GraphDSL.create(){
        implicit builder: GraphDSL.Builder[NotUsed] =>
          //1.
          val input = builder.add(source)
          val stateUpdater = builder.add(Flow[EventEnvelope].map(e=> updateState(e.event, e.sequenceNr)))
          val localSaveOutput = builder.add(Sink.foreach[Result]{
            r=>
              latestCalculatedResult = r.state
              println("something to print")
          })

          val dbSaveOutput = builder.add(
            Slick.sink[Result](r=> updatedResultAndOffset(r))
          )

          // надо разделить builder на 2 c помощью Broadcast
          //см https://blog.rockthejvm.com/akka-streams-graphs/

          //надо будет сохранить flow(разделенный на 2) в localSaveOutput и dbSaveOutput
          //в конце закрыть граф и запустить его RunnableGraph.fromGraph(graph).run()

      }*/

  /*
  source
    .map { x =>
      println(x.toString())
      x
    }
    .runForeach {
      event =>
        event.event match {
          case Added(_, amount) =>
            latestCalculatedResult += amount
            updatedResultAndOffset(latestCalculatedResult, event.sequenceNr)
            println(s"Log from Added: $latestCalculatedResult")
          case Multiplied(_, amount) =>
            latestCalculatedResult *= amount
            updatedResultAndOffset(latestCalculatedResult, event.sequenceNr)
            println(s"Log from Multiplied: $latestCalculatedResult")
          case Divided(_, amount) =>
            latestCalculatedResult /= amount
            updatedResultAndOffset(latestCalculatedResult, event.sequenceNr)
            println(s"Log from Divided: $latestCalculatedResult")
        }
    }
*/

  private def updateState(event: Any, seqNum: Long): Result = {
    val newState: Double = event match {
      case Added(_, amount) =>
        println(s"Log from Added: $latestCalculatedResult")
        latestCalculatedResult + amount
      case Multiplied(_, amount) =>
        println(s"Log from Multiplied: $latestCalculatedResult")
        latestCalculatedResult * amount
      case Divided(_, amount) =>
        println(s"Log from Divided: $latestCalculatedResult")
        latestCalculatedResult / amount

    }
    Result(newState, seqNum)
  }

  val graph = GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      //1.
      val input: SourceShape[EventEnvelope] = builder.add(source)
      val stateUpdater: FlowShape[EventEnvelope, Result] = builder.add(Flow[EventEnvelope].map(e => updateState(e.event, e.sequenceNr)))
      val localSaveOutput: SinkShape[Result] = builder.add(Sink.foreach[Result] {
        r =>
          latestCalculatedResult = r.state
          println("something to print")
      })

      val dbSaveOutput: SinkShape[Result] = builder.add(
        // Slick.sink[Result](r => updatedResultAndOffset(r))
        Sink.foreach[Result] {
          r =>
            updatedResultAndOffset(r.state, r.offset + 1)
            println("something to print")
        }
      )

      // надо разделить builder на 2 c помощью Broadcast
      //см https://blog.rockthejvm.com/akka-streams-graphs/

      val broadcast = builder.add(Broadcast[Result](outputPorts = 2))

      input ~> stateUpdater ~> broadcast

      //надо будет сохранить flow(разделенный на 2) в localSaveOutput и dbSaveOutput
      broadcast.out(0) ~> localSaveOutput
      broadcast.out(1) ~> dbSaveOutput

      //в конце закрыть граф и запустить его RunnableGraph.fromGraph(graph).run()
      ClosedShape
  }

  private val runTheGraph: NotUsed = RunnableGraph.fromGraph(graph).run()

}
