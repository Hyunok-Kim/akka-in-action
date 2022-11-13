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

class DispatcherInitTest extends AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  val configuration = ConfigFactory.load("performance/dispatcher")
  implicit val system = ActorSystem("DispatcherTest", configuration)

  "System" must {
    "fails to perform" in {
      val nrMessages = 6000
      val nrWorkers = 100
      val statDuration = 15000 millis //((nrMessage * 10)+1000)/4 millis

      val printer = system.actorOf(Props[PrintMsg]().withDispatcher("my-pinned-dispatcher"), "Printer-fail")
      val stat = system.actorOf(Props(new MonitorStatisticsActor(period = statDuration, processMargin = 1000,
        storeSummaries = printer)).withDispatcher("my-pinned-dispatcher"), "stat-fail")
      system.eventStream.subscribe(
        stat,
        classOf[ActorStatistics])
      system.eventStream.subscribe(
        stat,
        classOf[MailboxStatistics])

      val end = TestProbe()

      val workers = system.actorOf(
        RoundRobinPool(nrWorkers).props(
          Props(new ProcessRequest(1 second, end.ref) with MonitorActor)),
        "Workers-fail")

      val firstStep = system.actorOf(Props(new ProcessRequest(10 millis, workers) with MonitorActor), "Entry")

      for (i <- 0 until nrMessages) {
        firstStep ! new SystemMessage()
        Thread.sleep(15)
      }
      Thread.sleep(10000)
      printer ! "print"
      //end.receiveN(n =  nrMessages, max = 30 seconds)
      end.receiveN(n =  nrMessages, max = 3000 seconds)

      system.stop(firstStep)
      system.stop(workers)
      system.stop(stat)
      system.stop(printer)
    }
  }
}

//!!!!!!PRINT!!!!!!!! nr=7
//ENTRY: maxQueue=0, utilization 1.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1907416116],1668301320000,1668301335000,0,0.0,0.0,1.0,10.0)
//ENTRY: maxQueue=0, utilization 8.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1907416116],1668301335000,1668301350000,0,0.0,0.0,8.0,10.0)
//ENTRY: maxQueue=0, utilization 8.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1907416116],1668301350000,1668301365000,0,0.0,0.0,8.0,10.0)
//ENTRY: maxQueue=0, utilization 7.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1907416116],1668301365000,1668301380000,0,0.0,0.0,7.0,10.0)
//ENTRY: maxQueue=0, utilization 7.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1907416116],1668301380000,1668301395000,0,0.0,0.0,7.0,10.0)
//ENTRY: maxQueue=0, utilization 8.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1907416116],1668301395000,1668301410000,0,0.0,0.0,8.0,10.0)
//ENTRY: maxQueue=0, utilization 8.000000: StatisticsSummary(Actor[akka://DispatcherTest/user/Entry#-1907416116],1668301410000,1668301425000,0,0.0,0.0,8.0,10.0)

