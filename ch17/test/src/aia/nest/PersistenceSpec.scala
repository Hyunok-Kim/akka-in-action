package akka.testkit

import com.typesafe.config._

import scala.util._

import akka.actor._
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers

abstract class PersistenceSpec(system: ActorSystem) extends TestKit(system)
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  def this(name: String, config: Config) = this(ActorSystem(name, config))

  override protected def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  def killActors(actors: ActorRef*) = {
    actors.foreach { actor =>
      watch(actor)
      system.stop(actor)
      expectTerminated(actor)
      Thread.sleep(1000) // the actor name is not unique intermittently on travis when creating it again after killActors, this is ducktape.
    }
  }
}
