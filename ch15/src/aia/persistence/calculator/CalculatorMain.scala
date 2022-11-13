package aia.persistence.calculator

import akka.actor._
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.Await

object CalculatorMain extends App {
  val system = ActorSystem("calc")
  val calc = system.actorOf(Calculator.props, Calculator.name)

  calc ! Calculator.Add(1)
  calc ! Calculator.Multiply(3)
  calc ! Calculator.Divide(4)
  calc ! Calculator.PrintResult
  val f = (calc ? Calculator.GetResult)(1 second).mapTo[Double]
  println(Await.result(f, 1 second))

  system.terminate()
}
