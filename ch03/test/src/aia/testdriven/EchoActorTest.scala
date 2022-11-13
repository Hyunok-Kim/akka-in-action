package aia.testdriven

import akka.testkit.{TestKit, ImplicitSender}
import akka.actor.{Props, Actor, ActorSystem}
import org.scalatest.wordspec.AnyWordSpecLike

import akka.util.Timeout
import scala.concurrent.Await
import scala.util.{Success, Failure}

import scala.language.postfixOps

class EchoActorTest extends TestKit(ActorSystem("testsystem"))
  with AnyWordSpecLike
  with ImplicitSender
  with StopSystemAfterAll {

  "An EchoActor" must {
    "Reply with the same message it receives" in {

      import akka.pattern.ask
      import scala.concurrent.duration._
      implicit val timeout = Timeout(3 seconds)
      implicit val ec = system.dispatcher
      val echo = system.actorOf(Props[EchoActor](), "echo1")
      val future = echo.ask("some message")
      future.onComplete {
        case Failure(_) => // 실패 처리
        case Success(msg) => // 성공 처리
      }
      Await.ready(future, timeout.duration)
    }

    "Reply with the same message it receives without ask" in {
      val echo = system.actorOf(Props[EchoActor](), "echo2")
      echo ! "some message"
      expectMsg("some message")
    }
  }
}

class EchoActor extends Actor {
  def receive = {
    case msg =>
      sender() ! msg
  }
}
