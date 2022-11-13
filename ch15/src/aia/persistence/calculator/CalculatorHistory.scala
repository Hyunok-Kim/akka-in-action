package aia.persistence.calculator

import akka.actor._

import akka.persistence.query.{PersistenceQuery, EventEnvelope}
import com.fgrutsch.akka.persistence.mapdb.query.scaladsl.MapDbReadJournal

import akka.stream.scaladsl.Sink

object Payload {
  import Calculator._
  def unapply(arg: EventEnvelope): Option[Any] = Some(arg.event)
}

object CalculatorHistory {
  def props = Props(new CalculatorHistory)
  def name = "calculator-history"
  case object GetHistory
  case class History(added: Int = 0, subtracted: Int = 0, divided: Int = 0, multiplied: Int = 0) {
    def incrementAdded = copy(added = added + 1)
    def incrementSubtracted = copy(subtracted = subtracted + 1)
    def incrementDivided = copy(divided = divided + 1)
    def incrementMultiplied = copy(multiplied = multiplied + 1)
  }
}

class CalculatorHistory extends Actor with ActorLogging{
  import Calculator._
  import CalculatorHistory._

  val queries = PersistenceQuery(context.system).readJournalFor[MapDbReadJournal](MapDbReadJournal.Identifier)
  implicit val materializer = context.system
  queries.eventsByPersistenceId(Calculator.name, 0, Long.MaxValue).runWith(Sink.actorRef(self, None, _ => None))

  var history = History()

  def receive = {
    case Payload(Added(_))  => history = history.incrementAdded
    case Payload(Subtracted(_)) => history = history.incrementSubtracted
    case Payload(Divided(_)) => history = history.incrementDivided
    case Payload(Multiplied(_)) => history = history.incrementMultiplied
    case GetHistory => sender() ! history
  }
}
