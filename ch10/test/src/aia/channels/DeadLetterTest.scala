package aia.channels

import akka.testkit.{ImplicitSender, TestProbe, TestKit}
import akka.actor.{PoisonPill, Props, DeadLetter, ActorSystem}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import java.util.Date

class DeadLetterTest extends TestKit(ActorSystem("DeadLetterTest"))
  with AnyWordSpecLike with BeforeAndAfterAll with Matchers
  with ImplicitSender {

  override def afterAll() = {
    system.terminate()
  }

  "DeadLetter" must {
    "catch messages send to deadLetters" in {
      val deadLetterMonitor = TestProbe()

      system.eventStream.subscribe(
        deadLetterMonitor.ref,
        classOf[DeadLetter])

      val msg = new StateEvent(new Date(), "Connected")
      system.deadLetters ! msg

      val dead = deadLetterMonitor.expectMsgType[DeadLetter]
      dead.message must be(msg)
      dead.sender must be(testActor)
      dead.recipient must be(system.deadLetters)
    }
    "catch deadLetter messages send to deadLetters" in {

      val deadLetterMonitor = TestProbe()
      val actor = system.actorOf(Props[EchoActor](), "echo")

      system.eventStream.subscribe(
        deadLetterMonitor.ref,
        classOf[DeadLetter])

      val msg = new Order("me", "Akka in Action", 1)
      val dead = DeadLetter(msg, testActor, actor)
      system.deadLetters ! dead

      deadLetterMonitor.expectMsg(dead)

      system.stop(actor)
    }
    "catch messages send to terminated Actor" in {

      val deadLetterMonitor = TestProbe()

      system.eventStream.subscribe(
        deadLetterMonitor.ref,
        classOf[DeadLetter])

      val actor = system.actorOf(Props[EchoActor](), "echo")
      actor ! PoisonPill
      val msg = new Order("me", "Akka in Action", 1)
      actor ! msg

      val dead = deadLetterMonitor.expectMsgType[DeadLetter]
      dead.message must be(msg)
      dead.sender must be(testActor)
      dead.recipient must be(actor)
    }
  }
}
