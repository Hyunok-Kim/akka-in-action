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

class DispatcherThreadsTest extends AnyWordSpecLike
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

      val printer = system.actorOf(Props[PrintMsg](), "Printer-threads")
      val stat = system.actorOf(Props(new MonitorStatisticsActor(period = statDuration, processMargin = 1000,
        storeSummaries = printer)).withDispatcher("my-pinned-dispatcher"), "state-threads")
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
          "Workers-threads")

      val firstStep = system.actorOf(
        Props(new ProcessRequest(10 millis, workers) with MonitorActor).
          withDispatcher("my-pinned-dispatcher"), "Entry")

      for (i <- 0 until nrMessages) {
        firstStep ! new SystemMessage()
        Thread.sleep(15)
      }
      Thread.sleep(10000)
      printer ! "print"
      //val msg = end.receiveN(n = nrMessages, max = 30 seconds)
      //val msg = end.receiveN(n = nrMessages, max = 3000 seconds)

      system.stop(firstStep)
      system.stop(workers)
      system.stop(stat)
      system.stop(printer)
    }
  }
}

//!!!!!!PRINT!!!!!!!! nr=6
//ENTRY: maxQueue=2, utilization 29.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-2089807039],1668307770000,1668307785000,2,29.0,0.0,29.0,10.0)
//ENTRY: maxQueue=1, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-2089807039],1668307785000,1668307800000,1,65.0,0.0,67.0,10.0)
//ENTRY: maxQueue=1, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-2089807039],1668307800000,1668307815000,1,65.0,0.0,67.0,10.0)
//ENTRY: maxQueue=1, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-2089807039],1668307815000,1668307830000,1,65.0,0.0,67.0,10.0)
//ENTRY: maxQueue=1, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-2089807039],1668307830000,1668307845000,1,65.0,0.0,67.0,10.0)
//ENTRY: maxQueue=1, utilization 67.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-2089807039],1668307845000,1668307860000,1,65.0,0.0,67.0,10.0)
//
