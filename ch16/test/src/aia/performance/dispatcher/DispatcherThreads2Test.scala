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

class DispatcherThreads2Test extends AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val configuration = ConfigFactory.load("performance/dispatcher")
  implicit val system = ActorSystem("DispatcherTest", configuration)

  "System" must {
    "Use multiple threads" in {
      val nrMessages = 6000
      val nrWorkers = 100
      val statDuration = 15000 millis

      val printer = system.actorOf(Props[PrintMsg](), "Printer-threads2")
      val stat = system.actorOf(Props(new MonitorStatisticsActor(period = statDuration, processMargin = 1000,
        storeSummaries = printer)).withDispatcher("my-pinned-dispatcher"), "stat-threads2")
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
            withDispatcher("myMultiThread-dispatcher")),
          "Workers")

      val firstStep = system.actorOf(Props(new ProcessRequest(10 millis, workers) with MonitorActor).withDispatcher("myMultiThread-dispatcher"), "Entry")

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
//ENTRY: maxQueue=3, utilization 52.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#1690504156],1668308865000,1668308880000,3,51.0,0.0,52.0,10.0)
//ENTRY: maxQueue=1, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#1690504156],1668308880000,1668308895000,1,65.0,0.0,67.0,10.0)
//ENTRY: maxQueue=1, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#1690504156],1668308895000,1668308910000,1,65.0,0.0,67.0,10.0)
//ENTRY: maxQueue=1, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#1690504156],1668308910000,1668308925000,1,65.0,0.0,67.0,10.0)
//ENTRY: maxQueue=1, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#1690504156],1668308925000,1668308940000,1,65.0,0.0,67.0,10.0)
//ENTRY: maxQueue=1, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#1690504156],1668308940000,1668308955000,1,65.0,0.0,67.0,10.0)

