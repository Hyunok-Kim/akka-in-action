package aia.channels

import akka.testkit.{TestProbe, TestKit}
import akka.actor.ActorSystem
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.must.Matchers
import org.scalatest.BeforeAndAfterAll
import java.util.Date
import scala.concurrent.duration._

class CancelOrder(time: Date,
                override val customerId: String,
                override val productId: String,
                override val number: Int)
  extends Order(customerId, productId, number)

class EventStreamTest extends TestKit(ActorSystem("EventStreamTest"))
  with AnyWordSpecLike with BeforeAndAfterAll with Matchers {

  override def afterAll(): Unit = {
    system.terminate()
  }

  "EventStream" must {
    "distribute messages" in {
      val deliverOrder = TestProbe()
      val giftModule = TestProbe()

      system.eventStream.subscribe(
        deliverOrder.ref,
        classOf[Order])
      system.eventStream.subscribe(
        giftModule.ref,
        classOf[Order])

      val msg = new Order(
        customerId = "me",
        productId = "Akka in Action",
        number = 2)
      system.eventStream.publish(msg)
      
      deliverOrder.expectMsg(msg)
      giftModule.expectMsg(msg)
    }
    "monitor hierarchy" in {
      val giftModule = TestProbe()

      system.eventStream.subscribe(
        giftModule.ref,
        classOf[Order])

      val msg = new Order("me", "Akka in Action", 3)
      system.eventStream.publish(msg)

      giftModule.expectMsg(msg)

      val msg2 = new CancelOrder(new Date(), "me", "Akka in Action", 2)
      system.eventStream.publish(msg2)

      giftModule.expectMsg(msg2)
    }
    "Ignore other messages" in {
      val giftModule = TestProbe()

      system.eventStream.subscribe(
        giftModule.ref,
        classOf[CancelOrder])
      val msg = new Order("me", "Akka in Action", 3)
      system.eventStream.publish(msg)
      giftModule.expectNoMessage(3 seconds)
    }
    "unscribe messages" in {

      val DeliverOrder = TestProbe()
      val giftModule = TestProbe()

      system.eventStream.subscribe(
        DeliverOrder.ref,
        classOf[Order])
      system.eventStream.subscribe(
        giftModule.ref,
        classOf[Order])

      val msg = new Order("me", "Akka in Action", 3)
      system.eventStream.publish(msg)

      DeliverOrder.expectMsg(msg)
      giftModule.expectMsg(msg)

      system.eventStream.unsubscribe(giftModule.ref)

      system.eventStream.publish(msg)
      DeliverOrder.expectMsg(msg)
      giftModule.expectNoMessage(3 seconds)
    }
  }
  "MyEventBus" must {
    "deliver all messages" in {
      val bus = new MyEventBus
      val systemLog = TestProbe()
      bus.subscribe(systemLog.ref)
      val msg = new Order("me", "Akka in Action", 3)
      bus.publish(msg)
      systemLog.expectMsg(msg)

      bus.publish("test")
      systemLog.expectMsg("test")
    }
  }
}
