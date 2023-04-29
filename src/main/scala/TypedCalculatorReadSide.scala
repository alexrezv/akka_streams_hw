import CalculatorRepository.{getLatestOffsetAndResult, saveToDbSink}
import Event._
import TypedCalculatorMain.persistenceId
import akka.actor.typed.ActorSystem
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.alpakka.slick.scaladsl.SlickSession
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ClosedShape, FlowShape, SinkShape, SourceShape}
import akka.{NotUsed, actor}

case class TypedCalculatorReadSide(system: ActorSystem[NotUsed])(implicit session: SlickSession) {

  implicit val materializer: actor.ActorSystem = system.classicSystem

  var (id, offset, latestCalculatedResult) = {
    val r = getLatestOffsetAndResult
    (r.id, r.offset, r.state)
  }
  private val startOffset: Long = if (offset == 1) 1 else offset + 1

  private val readJournal = PersistenceQuery(system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  private val source: Source[EventEnvelope, NotUsed] = readJournal.eventsByPersistenceId(persistenceId.id, startOffset, Long.MaxValue)

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
    Result(id, newState, seqNum)
  }

  private val graph = GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._

      val input: SourceShape[EventEnvelope] = builder.add(source)
      val stateUpdater: FlowShape[EventEnvelope, Result] = builder.add(Flow[EventEnvelope].map(e => updateState(e.event, e.sequenceNr)))
      val localSaveOutput: SinkShape[Result] = builder.add(Sink.foreach[Result] {
        r =>
          println(s"Local save of $r")
          latestCalculatedResult = r.state
      })

      val dbSaveOutput: SinkShape[Result] = builder.add(saveToDbSink)

      val broadcast = builder.add(Broadcast[Result](outputPorts = 2))

      input ~> stateUpdater ~> broadcast

      broadcast.out(0) ~> localSaveOutput
      broadcast.out(1) ~> dbSaveOutput

      ClosedShape
  }

  RunnableGraph.fromGraph(graph).run()

}
