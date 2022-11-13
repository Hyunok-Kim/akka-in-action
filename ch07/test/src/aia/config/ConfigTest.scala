package aia.config

import akka.actor.ActorSystem
import org.scalatest.wordspec.AnyWordSpecLike
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.must.Matchers

class ConfigTest extends AnyWordSpecLike with Matchers {

  "Configuration" must {
    "has configuration" in {
      val mySystem = ActorSystem("myTest")
      val config = mySystem.settings.config
      config.getInt("myTest.intParam") must be(20)
      config.getString("myTest.applicationDesc") must be("My Config Test")
    }
    "has defaults" in {
      val mySystem = ActorSystem("myDefaultsTest")
      val config = mySystem.settings.config
      config.getInt("myTestDefaults.intParam") must be(20)
      config.getString("myTestDefaults.applicationDesc") must be("My Current Test")
    }
    "can include file" in {
      val mySystem = ActorSystem("myIncludeTest")
      val  config = mySystem.settings.config
      config.getInt("myTestIncluded.intParam") must be(20)
      config.getString("myTestIncluded.applicationDesc") must be("My Include Test")
    }
    "can be loaded by ourself" in {
      val configuration = ConfigFactory.load("load")
      val mySystem = ActorSystem("myLoadTest", configuration)
      val config = mySystem.settings.config
      config.getInt("myTestLoad.intParam") must be(20)
      config.getString("myTestLoad.applicationDesc") must be("My Load Test")
    }
  }
}
