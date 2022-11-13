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

class DispatcherThreadPoolTest extends AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val configuration = ConfigFactory.load("performance/dispatcher")
  implicit val system = ActorSystem("DispatcherTest", configuration)

  "System" must {
    "fail to perform threadPool" in {
      val nrMessages = 6000
      val nrWorkers = 100
      val statDuration = 15000 millis

      val printer = system.actorOf(Props[PrintMsg](), "Printer-threadpool")
      val stat = system.actorOf(Props(new MonitorStatisticsActor(period = statDuration, processMargin = 1000,
        storeSummaries = printer)), "stat-threadpool")
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
            withDispatcher("my-thread-dispatcher")),
          "Workers")

      val firstStep = system.actorOf(Props(new ProcessRequest(10 millis, workers) with MonitorActor), "Entry")

      for (i <- 0 until nrMessages) {
        firstStep ! new SystemMessage()
        Thread.sleep(15)
      }
      Thread.sleep(10000)
      printer ! "print"
      val msg = end.receiveN(n = nrMessages, max = 30 seconds)

      system.stop(firstStep)
      system.stop(workers)
      system.stop(stat)
      system.stop(printer)
    }
  }
}

//!!!!!!PRINT!!!!!!!! nr=6
//ENTRY: maxQueue=0, utilization 66.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-784712780],1668309885000,1668309900000,0,0.0,0.0,66.0,10.0)
//ENTRY: maxQueue=0, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-784712780],1668309900000,1668309915000,0,0.0,0.0,67.0,10.0)
//ENTRY: maxQueue=0, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-784712780],1668309915000,1668309930000,0,0.0,0.0,67.0,10.0)
//ENTRY: maxQueue=0, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-784712780],1668309930000,1668309945000,0,0.0,0.0,67.0,10.0)
//ENTRY: maxQueue=0, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-784712780],1668309945000,1668309960000,0,0.0,0.0,67.0,10.0)
//ENTRY: maxQueue=0, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-784712780],1668309960000,1668309975000,0,0.0,0.0,67.0,10.0)
