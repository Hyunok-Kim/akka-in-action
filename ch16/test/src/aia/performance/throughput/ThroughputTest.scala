package aia.performance.throughput

import akka.testkit.TestProbe
import akka.actor.{Props, ActorSystem}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import akka.routing.RoundRobinPool
import com.typesafe.config.ConfigFactory
import aia.performance.{ProcessCPURequest, SystemMessage, ProcessRequest}
import concurrent.duration._

class ThroughputTest extends AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val configuration = ConfigFactory.load("performance/through")
  implicit val system = ActorSystem("ThroughputTest", configuration)

  "System" must {
    "fails to perform" in {
      val nrMessages = 99
      val nrWorkers = 3
      val statDuration = 2000 millis //((nrMessages * 10)+1000)/4 millis

      val end = TestProbe()
      val workers = system.actorOf(
        RoundRobinPool(nrWorkers).props(Props(new ProcessRequest(1 second, end.ref)).withDispatcher("my-dispatcher")),
        "Workers")

      val startTime = System.currentTimeMillis()
      for (i <- 0 until nrMessages) {
        workers ! new SystemMessage(startTime, 0, "")
      }
      val msg = end.receiveN(n = nrMessages, max = 9000 seconds).asInstanceOf[Seq[SystemMessage]]
      val endTime = System.currentTimeMillis()
      val total = endTime - startTime
      println("total process time %d Average=%d".format(total, total / nrMessages))

      val grouped = msg.groupBy(_.id)
      grouped.map {
        case (key, listMsg) => (key, listMsg.foldLeft(0L) { (m, x) => math.max(m, x.duration) })
      }.foreach(println(_))

      Thread.sleep(1000)

      system.stop(workers)
    }
  }
}

//total process time 33070 Average=334
//(akka://ThroughputTest/user/Workers/$c,33067)
//(akka://ThroughputTest/user/Workers/$b,33068)
//(akka://ThroughputTest/user/Workers/$a,33068)
