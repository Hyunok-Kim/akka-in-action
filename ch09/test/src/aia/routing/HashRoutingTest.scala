package aia.routing

import scala.concurrent.duration._

import akka.actor._
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll
import akka.routing._
import akka.routing.ConsistentHashingRouter._

import akka.testkit.{TestProbe, TestKit}

class HashRoutingTest extends TestKit(ActorSystem("PerfRoutingTest"))
  with AnyWordSpecLike with BeforeAndAfterAll {

  override def afterAll() = {
    system.terminate()
  }

  "The HashRouting" must {
    "work using mapping" in {
      val endProbe = TestProbe()

      def hashMapping: ConsistentHashMapping = {
        case msg: GatherMessage => msg.id
      }

      val router = system.actorOf(ConsistentHashingPool(10, virtualNodesFactor = 10, hashMapping = hashMapping).
        props(Props(new SimpleGather(endProbe.ref))), name = "routerMapping")

      router ! GatherMessageNormalImpl("1", Seq("msg1"))
      endProbe.expectNoMessage(100.millis)
      router ! GatherMessageNormalImpl("1", Seq("msg2"))
      endProbe.expectMsg(GatherMessageNormalImpl("1", Seq("msg1", "msg2")))

      router ! GatherMessageNormalImpl("10", Seq("msg1"))
      endProbe.expectNoMessage(100.millis)
      router ! GatherMessageNormalImpl("10", Seq("msg2"))
      endProbe.expectMsg(GatherMessageNormalImpl("10", Seq("msg1", "msg2")))
      system.stop(router)
    }
    "work using messages" in {
      val endProbe = TestProbe()

      val router = system.actorOf(ConsistentHashingPool(10, virtualNodesFactor = 10).
        props(Props(new SimpleGather(endProbe.ref))), name = "routerMessage")

      router ! GatherMessageWithHash("1", Seq("msg1"))
      endProbe.expectNoMessage(100.millis)
      router ! GatherMessageWithHash("1", Seq("msg2"))
      endProbe.expectMsg(GatherMessageNormalImpl("1", Seq("msg1", "msg2")))

      router ! GatherMessageWithHash("10", Seq("msg1"))
      endProbe.expectNoMessage(100.millis)
      router ! GatherMessageWithHash("10", Seq("msg2"))
      endProbe.expectMsg(GatherMessageNormalImpl("10", Seq("msg1", "msg2")))
      system.stop(router)
      Thread.sleep(100)
    }
    "work using Envelope" in {
      val endProbe = TestProbe()

      val router = system.actorOf(ConsistentHashingPool(10, virtualNodesFactor = 10).
        props(Props(new SimpleGather(endProbe.ref))), name = "routerMessage")

      router ! ConsistentHashableEnvelope(
        message = GatherMessageNormalImpl("1", Seq("msg1")),
        hashKey = "someHash")

      endProbe.expectNoMessage(100.millis)
      router ! ConsistentHashableEnvelope(
        message = GatherMessageNormalImpl("1", Seq("msg2")),
        hashKey = "someHash")
      endProbe.expectMsg(GatherMessageNormalImpl("1", Seq("msg1", "msg2")))

      router ! ConsistentHashableEnvelope(
        message = GatherMessageNormalImpl("10", Seq("msg1")),
        hashKey = "10")
      endProbe.expectNoMessage(100.millis)
      router ! ConsistentHashableEnvelope(
        message = GatherMessageNormalImpl("10", Seq("msg2")),
        hashKey = "10")
      endProbe.expectMsg(GatherMessageNormalImpl("10", Seq("msg1", "msg2")))
      system.stop(router)
      Thread.sleep(100)
    }
    "fail without using hash" in {
      val endProbe = TestProbe()

      val router = system.actorOf(ConsistentHashingPool(10, virtualNodesFactor = 10).
        props(Props(new SimpleGather(endProbe.ref))), name = "routerMessage")

      router ! GatherMessageNormalImpl("1", Seq("msg1"))
      endProbe.expectNoMessage(100.millis)
      router ! GatherMessageNormalImpl("1", Seq("msg2"))

      system.stop(router)
    }
  }
}
