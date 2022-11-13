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

class ThroughputServiceTimeTest extends AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val configuration = ConfigFactory.load("performance/through")
  implicit val system = ActorSystem("ThroughputTest", configuration)

  "System" must {
    "fails to with low service time" in {
      val nrWorkers = 40
      val nrMessages = nrWorkers * 40 * 1000

      val end = TestProbe()
      val workers = system.actorOf(
        RoundRobinPool(nrWorkers).props(
          Props(new ProcessRequest(0 millis, end.ref)).withDispatcher("my-dispatcher")),
        "Workers-service")

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

//total process time 4769 Average=0
//(akka://ThroughputTest/user/Workers-service/$A,3308)
//(akka://ThroughputTest/user/Workers-service/$q,2535)
//(akka://ThroughputTest/user/Workers-service/$c,4351)
//(akka://ThroughputTest/user/Workers-service/$u,1361)
//(akka://ThroughputTest/user/Workers-service/$B,4743)
//(akka://ThroughputTest/user/Workers-service/$p,4398)
//(akka://ThroughputTest/user/Workers-service/$y,2487)
//(akka://ThroughputTest/user/Workers-service/$j,3692)
//(akka://ThroughputTest/user/Workers-service/$w,4724)
//(akka://ThroughputTest/user/Workers-service/$N,1791)
//(akka://ThroughputTest/user/Workers-service/$z,4371)
//(akka://ThroughputTest/user/Workers-service/$K,2924)
//(akka://ThroughputTest/user/Workers-service/$h,1696)
//(akka://ThroughputTest/user/Workers-service/$k,2539)
//(akka://ThroughputTest/user/Workers-service/$v,4769)
//(akka://ThroughputTest/user/Workers-service/$H,4013)
//(akka://ThroughputTest/user/Workers-service/$G,3252)
//(akka://ThroughputTest/user/Workers-service/$g,4058)
//(akka://ThroughputTest/user/Workers-service/$C,4416)
//(akka://ThroughputTest/user/Workers-service/$r,1766)
//(akka://ThroughputTest/user/Workers-service/$i,3648)
//(akka://ThroughputTest/user/Workers-service/$F,2163)
//(akka://ThroughputTest/user/Workers-service/$x,1736)
//(akka://ThroughputTest/user/Workers-service/$f,1357)
//(akka://ThroughputTest/user/Workers-service/$I,2137)
//(akka://ThroughputTest/user/Workers-service/$l,2898)
//(akka://ThroughputTest/user/Workers-service/$b,2934)
//(akka://ThroughputTest/user/Workers-service/$L,4718)
//(akka://ThroughputTest/user/Workers-service/$e,2564)
//(akka://ThroughputTest/user/Workers-service/$E,2072)
//(akka://ThroughputTest/user/Workers-service/$t,1382)
//(akka://ThroughputTest/user/Workers-service/$M,4008)
//(akka://ThroughputTest/user/Workers-service/$m,2176)
//(akka://ThroughputTest/user/Workers-service/$J,3961)
//(akka://ThroughputTest/user/Workers-service/$d,3249)
//(akka://ThroughputTest/user/Workers-service/$o,3632)
//(akka://ThroughputTest/user/Workers-service/$D,2893)
//(akka://ThroughputTest/user/Workers-service/$s,1293)
//(akka://ThroughputTest/user/Workers-service/$a,3635)
//(akka://ThroughputTest/user/Workers-service/$n,3274)
