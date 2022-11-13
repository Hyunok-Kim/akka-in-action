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

class ThroughputCPUTest extends AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val configuration = ConfigFactory.load("performance/through")
  implicit val system = ActorSystem("ThroughputTest", configuration)

  "System" must {
    "fails to with cpu" in {
      val nrWorkers = 40
      val nrMessages = nrWorkers * 40

      val end = TestProbe()
      val workers = system.actorOf(
        RoundRobinPool(nrWorkers).props(
          Props(new ProcessCPURequest(250 millis, end.ref)).withDispatcher("my-dispatcher")),
        "Workers-cpu")

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

//total process time 100035 Average=62
//(akka://ThroughputTest/user/Workers-cpu/$l,30033)
//(akka://ThroughputTest/user/Workers-cpu/$D,80032)
//(akka://ThroughputTest/user/Workers-cpu/$z,70032)
//(akka://ThroughputTest/user/Workers-cpu/$e,20031)
//(akka://ThroughputTest/user/Workers-cpu/$j,30031)
//(akka://ThroughputTest/user/Workers-cpu/$M,100033)
//(akka://ThroughputTest/user/Workers-cpu/$t,50033)
//(akka://ThroughputTest/user/Workers-cpu/$c,10031)
//(akka://ThroughputTest/user/Workers-cpu/$x,60033)
//(akka://ThroughputTest/user/Workers-cpu/$C,80031)
//(akka://ThroughputTest/user/Workers-cpu/$H,90032)
//(akka://ThroughputTest/user/Workers-cpu/$b,10031)
//(akka://ThroughputTest/user/Workers-cpu/$K,100031)
//(akka://ThroughputTest/user/Workers-cpu/$i,30031)
//(akka://ThroughputTest/user/Workers-cpu/$A,70035)
//(akka://ThroughputTest/user/Workers-cpu/$v,60031)
//(akka://ThroughputTest/user/Workers-cpu/$E,80033)
//(akka://ThroughputTest/user/Workers-cpu/$r,50031)
//(akka://ThroughputTest/user/Workers-cpu/$w,60031)
//(akka://ThroughputTest/user/Workers-cpu/$a,10031)
//(akka://ThroughputTest/user/Workers-cpu/$I,90033)
//(akka://ThroughputTest/user/Workers-cpu/$k,30031)
//(akka://ThroughputTest/user/Workers-cpu/$N,100035)
//(akka://ThroughputTest/user/Workers-cpu/$o,40031)
//(akka://ThroughputTest/user/Workers-cpu/$f,20031)
//(akka://ThroughputTest/user/Workers-cpu/$s,50031)
//(akka://ThroughputTest/user/Workers-cpu/$F,80035)
//(akka://ThroughputTest/user/Workers-cpu/$q,50031)
//(akka://ThroughputTest/user/Workers-cpu/$g,20031)
//(akka://ThroughputTest/user/Workers-cpu/$J,90035)
//(akka://ThroughputTest/user/Workers-cpu/$n,40031)
//(akka://ThroughputTest/user/Workers-cpu/$m,40031)
//(akka://ThroughputTest/user/Workers-cpu/$p,40033)
//(akka://ThroughputTest/user/Workers-cpu/$d,10031)
//(akka://ThroughputTest/user/Workers-cpu/$y,70031)
//(akka://ThroughputTest/user/Workers-cpu/$u,60031)
//(akka://ThroughputTest/user/Workers-cpu/$h,20033)
//(akka://ThroughputTest/user/Workers-cpu/$B,70033)
//(akka://ThroughputTest/user/Workers-cpu/$G,90031)
//(akka://ThroughputTest/user/Workers-cpu/$L,100032)
