package aia.persistence.calculator

import akka.actor._
import akka.testkit._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import akka.persistence.query.{PersistenceQuery,EventEnvelope,Sequence}
import com.fgrutsch.akka.persistence.mapdb.db.MapDbExtension
import com.fgrutsch.akka.persistence.mapdb.query.scaladsl.MapDbReadJournal
import com.fgrutsch.akka.persistence.mapdb.journal.{JournalConfig, MapDbJournalRepository}
import akka.stream.scaladsl._
import Calculator._

class CalculatorSpec extends PersistenceSpec(ActorSystem("test"))
  with PersistenceCleanup {

  "The Calculator" should {
    "recover last known result after crash" in {
      val calc = system.actorOf(Calculator.props, Calculator.name)
      calc ! Calculator.Add(1d)
      calc ! Calculator.GetResult
      expectMsg(1d)

      calc ! Calculator.Subtract(0.5d)
      calc ! Calculator.GetResult
      expectMsg(0.5d)

      killActors(calc)

      /*
      val db = MapDbExtension(system).database
      val journalConfig = new JournalConfig(system.settings.config.getConfig("mapdb-journal"))
      val journalRepo = new MapDbJournalRepository(db, journalConfig.db)
      val readJournal = PersistenceQuery(system).readJournalFor[MapDbReadJournal](MapDbReadJournal.Identifier)
      val f1 = readJournal.currentEventsByPersistenceId("my-calculator", 0, Long.MaxValue).map(_ => 1).runFold(0)(_ + _)
      val nq = Await.result(f1, 1.second)
      val tp = readJournal.eventsByPersistenceId("my-calculator", 0, Long.MaxValue)
      val queue = tp.runWith(Sink.queue())
      val evts = (for (_ <- 1 to nq) yield Await.result(queue.pull(), 1.second)) map {case Some(ev) => ev; case None => None }


      evts should be(
        Vector(EventEnvelope(Sequence(1),"my-calculator",1,Added(1.0),0,None),
        EventEnvelope(Sequence(2),"my-calculator",2,Subtracted(0.5),0,None))
      )
      */
      val queries = PersistenceQuery(system).readJournalFor[MapDbReadJournal](MapDbReadJournal.Identifier)
      queries.eventsByPersistenceId(Calculator.name, 0, Long.MaxValue).runWith(Sink.actorRef(self, None, _ => None))
      val evts = receiveN(2, 1 second).collect {
        case evt: EventEnvelope => evt
      }

      evts should be(
        Vector(EventEnvelope(Sequence(1),"my-calculator",1,Added(1.0),0,None),
        EventEnvelope(Sequence(2),"my-calculator",2,Subtracted(0.5),0,None))
      )

      val calcResurrected = system.actorOf(Calculator.props, Calculator.name)
      calcResurrected ! Calculator.GetResult
      expectMsg(0.5d)

      calcResurrected ! Calculator.Add(1d)
      calcResurrected ! Calculator.GetResult
      expectMsg(1.5d)
    }
  }
}
