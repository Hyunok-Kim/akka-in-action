package aia.integration

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import collection.mutable

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._

import scala.xml.{Elem, XML, NodeSeq}
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._

case class Order(customerId: String, productId: String, number: Int)
case class TrackingOrder(id: Long, status: String, order: Order)
case class OrderId(id: Long)
case class NoSuchOrder(id: Long)

class ProcessOrders extends Actor {

  val orderList = new mutable.HashMap[Long, TrackingOrder]()
  var lastOrderId = 0L

  def receive = {
    case order: Order =>
      lastOrderId += 1
      val newOrder = new TrackingOrder(lastOrderId, "received", order)
      orderList += lastOrderId -> newOrder
      sender() ! newOrder
    case order: OrderId =>
      orderList.get(order.id) match {
        case Some(intOrder) =>
          sender() ! intOrder.copy(status = "processing")
        case None => sender() ! NoSuchOrder(order.id)
      }
    case "reset" =>
      lastOrderId = 0
      orderList.clear()
  }
}

class OrderServiceApi(
  system: ActorSystem,
  timeout: Timeout,
  val processOrders: ActorRef
) extends OrderService {
  implicit val requestTimeout = timeout
  implicit def executionContext = system.dispatcher
}

trait OrderService {
  val processOrders: ActorRef

  implicit def executionContext: ExecutionContext
  implicit def requestTimeout: Timeout

  val routes = getOrder ~ postOrders

  def getOrder = get {
    pathPrefix("orders" / IntNumber) { id =>
      onSuccess(processOrders.ask(OrderId(id))) {
        case result: TrackingOrder =>
          complete(<statusResponse>
            <id>{ result.id }</id>
            <status>{ result.status }</status>
          </statusResponse>)
        case result: NoSuchOrder =>
          complete(StatusCodes.NotFound)
      }
    }
  }

  def postOrders = post {
    path("orders") {
      entity(as[NodeSeq]) { xml =>
        val order = toOrder(xml)
        onSuccess(processOrders.ask(order)) {
          case result: TrackingOrder =>
            complete(
              <confirm>
                <id>{ result.id }</id>
                <status>{ result.status }</status>
              </confirm>
            )

          case result =>
            complete(StatusCodes.BadRequest)
        }
      }
    }
  }

  def toOrder(xml: NodeSeq): Order = {
    val order = xml \\ "order"
    val customer = (order \\ "customerId").text
    val productId = (order \\ "productId").text
    val number = (order \\ "number").text.toInt
    new Order(customer, productId, number)
  }
}
