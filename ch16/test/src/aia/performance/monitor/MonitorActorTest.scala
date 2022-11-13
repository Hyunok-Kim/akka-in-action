package aia.performance.monitor

import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers

import akka.testkit.{TestProbe, TestKit}
import akka.actor.{Props, ActorSystem}
import concurrent.duration._

class MonitorActorTest extends TestKit(ActorSystem("MonitorActorTest"))
  with AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  "Actor" must {
    "send statistics" in {
      val statProbe = TestProbe()
      system.eventStream.subscribe(
        statProbe.ref,
        classOf[ActorStatistics])
      val testActor = system.actorOf(Props(
        new ProcessTestActor(1.second) with MonitorActor), "monitorActor")
      statProbe.send(testActor, "message")
      statProbe.send(testActor, "message2")
      statProbe.send(testActor, "message3")

      val stat = statProbe.expectMsgType[ActorStatistics]
      println(stat)
      stat.exitTime - stat.entryTime must be(1000L +- 20)
      val stat2 = statProbe.expectMsgType[ActorStatistics]
      println(stat2)
      stat2.exitTime - stat2.entryTime must be(1000L +- 20)
      val stat3 = statProbe.expectMsgType[ActorStatistics]
      println(stat3)
      stat3.exitTime - stat3.entryTime must be(1000L +- 20)

      Thread.sleep(2000)
      system.stop(testActor)
      system.eventStream.unsubscribe(statProbe.ref)
    }
  }
}
