package akka.testkit

import java.io.File
import com.typesafe.config._

import scala.util._

import akka.actor._
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterAll

import org.apache.commons.io.FileUtils

abstract class PersistenceSpec(system: ActorSystem) extends TestKit(system)
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with PersistenceCleanup {

  def this(name: String, config: Config) = this(ActorSystem(name, config))
  override protected def beforeAll() = deleteStorageLocations()

  override protected def afterAll() = {
    deleteStorageLocations()
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

trait PersistenceCleanup {
  def system: ActorSystem

  val storageLocations = List(
    "akka-persistence-mapdb_default"
  ).map { s => new File(s) }

  def deleteStorageLocations(): Unit = {
    storageLocations.foreach(file => Try(FileUtils.forceDelete(file)))
  }
}
