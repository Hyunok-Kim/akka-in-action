package aia.performance.dispatcher

import akka.testkit.TestProbe
import akka.actor.{Props, ActorSystem}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import akka.routing.RoundRobinPool
import com.typesafe.config.ConfigFactory
import aia.performance.{SystemMessage, ProcessRequest, PrintMsg}
import aia.performance.monitor.{MonitorActor, MailboxStatistics, ActorStatistics, MonitorStatisticsActor}
import concurrent.duration._

class DispatcherThroughputTest extends AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val configuration = ConfigFactory.load("performance/dispatcher")
  implicit val system = ActorSystem("DispatcherTest", configuration)

  "System" must {
    "fails to perform change throughput" in {
      val nrMessages = 6000
      val nrWorkers = 100
      val statDuration = 15000 millis

      val printer = system.actorOf(Props[PrintMsg]().withDispatcher("my-pinned-dispatcher"), "Printer-throughput")
      val stat = system.actorOf(Props(new MonitorStatisticsActor(period = statDuration, processMargin = 1000,
        storeSummaries = printer)).withDispatcher("my-pinned-dispatcher"), "stat-throughput")
      system.eventStream.subscribe(
        stat,
        classOf[ActorStatistics])
      system.eventStream.subscribe(
        stat,
        classOf[MailboxStatistics])

      val end = TestProbe()
      val workers = system.actorOf(
        RoundRobinPool(nrWorkers).props(
          Props(new ProcessRequest(1 second, end.ref) with MonitorActor).
            withDispatcher("throughput-dispatcher")),
          "Workers")

      val firstStep = system.actorOf(Props(new ProcessRequest(10 millis, workers) with MonitorActor).withDispatcher("throughput-dispatcher"), "Entry")

      for (i <- 0 until nrMessages) {
        firstStep ! new SystemMessage()
        Thread.sleep(15)
      }
      Thread.sleep(10000)
      printer ! "print"
      //val msg = end.receiveN(n = nrMessages, max = 30 seconds)
      val msg = end.receiveN(n = nrMessages, max = 3000 seconds)

      system.stop(firstStep)
      system.stop(workers)
      system.stop(stat)
      system.stop(printer)
    }
  }
}

//!!!!!!PRINT!!!!!!!! nr=7
//ENTRY: maxQueue=61, utilization 14.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#1131466716],1668311070000,1668311085000,61,13.0,450.0,14.0,10.0)
//ENTRY: maxQueue=0, utilization 68.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#1131466716],1668311100000,1668311115000,0,0.0,0.0,68.0,10.0)
