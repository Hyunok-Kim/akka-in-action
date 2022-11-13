package aia.persistence.calculator

import akka.actor._
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.Await

object CalculatorHistoryMain extends App {
  val system = ActorSystem("calc")
  val history = system.actorOf(CalculatorHistory.props, CalculatorHistory.name)

  Thread.sleep(1000)
  val f = (history ? CalculatorHistory.GetHistory)(1 second).mapTo[CalculatorHistory.History]
  println(Await.result(f, 1 second))

  system.terminate()
}
