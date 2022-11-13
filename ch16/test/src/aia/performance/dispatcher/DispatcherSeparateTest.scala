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

class DispatcherSeparateTest extends AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val configuration = ConfigFactory.load("performance/dispatcher")
  implicit val system = ActorSystem("DispatcherTest", configuration)

  "system" must {
    "fails to perform seperate dispatchers" in {
      val nrMessages = 6000
      val nrWorkers = 100
      val statDuration = 15000 millis

      val printer = system.actorOf(Props[PrintMsg](), "Printer-dispatcher")
      val stat = system.actorOf(Props(new MonitorStatisticsActor(period = statDuration, processMargin = 1000,
        storeSummaries = printer)), "stat-dispatcher")
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
            withDispatcher("my-dispatcher2")),
          "Workers")

      val firstStep = system.actorOf(Props(new ProcessRequest(10 millis, workers) with MonitorActor), "Entry")

      for (i <- 0 until nrMessages) {
        firstStep ! new SystemMessage()
        Thread.sleep(15)
      }
      Thread.sleep(10000)
      printer ! "print"
      //end.receiveN(n = nrMessages, max = 30 seconds)
      end.receiveN(n = nrMessages, max = 3000 seconds)

      system.stop(firstStep)
      system.stop(workers)
      system.stop(stat)
      system.stop(printer)
    }
  }
}

//!!!!!!PRINT!!!!!!!! nr=7
//ENTRY: maxQueue=0, utilization 10.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1995758718],1668306795000,1668306810000,0,0.0,0.0,10.0,10.0)
//ENTRY: maxQueue=0, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1995758718],1668306810000,1668306825000,0,0.0,0.0,67.0,10.0)
//ENTRY: maxQueue=0, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1995758718],1668306825000,1668306840000,0,0.0,0.0,67.0,10.0)
//ENTRY: maxQueue=0, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1995758718],1668306840000,1668306855000,0,0.0,0.0,67.0,10.0)
//ENTRY: maxQueue=0, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1995758718],1668306855000,1668306870000,0,0.0,0.0,67.0,10.0)
//ENTRY: maxQueue=0, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1995758718],1668306870000,1668306885000,0,0.0,0.0,67.0,10.0)
//ENTRY: maxQueue=0, utilization 62.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1995758718],1668306885000,1668306900000,0,0.0,0.0,62.0,10.0)

