import akka.Done
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.scaladsl.Sink
import slick.jdbc.GetResult

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

object CalculatorRepository {

  implicit val getResult: GetResult[Result] = GetResult(r => Result(r.nextLong(), r.nextDouble(), r.nextLong()))

  def getLatestOffsetAndResult(implicit session: SlickSession): Result = {
    import session.profile.api._
    val query = sql"select * from public.result where id = 1;".as[Result].headOption
    val f = session.db.run(query)
    Await.result(f, atMost = 3.seconds).get
  }

  def saveToDbSink(implicit session: SlickSession): Sink[Result, Future[Done]] = {
    import session.profile.api._
    Slick.sink[Result]((it: Result) => sqlu"update public.result set calculated_value = ${it.state}, write_side_offset = ${it.offset} where id = ${it.id}")
  }

}
