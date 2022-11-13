package aia.next

import akka.actor.typed._

import akka.actor.typed.scaladsl._

object TypedBasket {
  sealed trait Command {
    def shopperId: Long
  }

  final case class GetItems(shopperId: Long,
                            replyTo: ActorRef[Items]) extends Command
  final case class Add(item: Item, shopperId: Long) extends Command

  // a simplified version of Items and Item
  case class Items(list: Vector[Item] = Vector.empty[Item])
  case class Item(productId: String, number: Int, unitPrice: BigDecimal)

  def basketBehavior(items: Items = Items()): Behavior[Command] =
    Behaviors.receiveMessage { msg =>
      msg match {
        case GetItems(productId, replyTo) =>
          replyTo ! items
          Behaviors.same
        case Add(item, productId) =>
          basketBehavior(Items(items.list :+ item))
      }
    }
}
