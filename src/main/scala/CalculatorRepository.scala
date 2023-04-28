import scalikejdbc.DB.using
import scalikejdbc.{ConnectionPool, ConnectionPoolSettings, DB}

object CalculatorRepository {

  //homework how to do
  //1.
  /*
  def createSession(): SlickSession = {
    //создайте сессию согласно документации
  }
  */


  def initDatabase(): Unit = {
    Class.forName("org.postgresql.Driver")
    val poolSettings = ConnectionPoolSettings(initialSize = 10, maxSize = 100)
    ConnectionPool.singleton("jdbc:postgresql://localhost:5432/demo", "docker", "docker", poolSettings)
  }

  // homework
  // case class Result(state: Double, offset:Long)
  /*
  def getLatestsOffsetAndResult: Result = {
    val query = sql"select * from public.result where id = 1;".as[Double].headOption
    //надо создать future для db.run
    //с помошью await получите результат или прокиньте ошибку если результат нет
  }
  */


  def getLatestOffsetAndResult: (Int, Double) = {
    val entities =
      DB readOnly { session =>
        session.list("select * from public.result where id = 1;") {
          row =>
            (
              row.int("write_side_offset"),
              row.double("calculated_value"))
        }
      }
    entities.head
  }


  //homework how to do
  def updatedResultAndOffset(calculated: Double, offset: Long): Unit = {
    using(DB(ConnectionPool.borrow())) {
      db =>
        db.autoClose(true)
        db.localTx {
          _.update("update public.result set calculated_value = ?, write_side_offset = ? where id = 1"
            , calculated, offset)
        }
    }
  }

}
