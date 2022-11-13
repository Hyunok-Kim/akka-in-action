import mill._, scalalib._
import $file.plugins

object ch02 extends ScalaModule {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def akkaHttpVersion = "10.2.10"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-stream:${akkaVersion}",
    ivy"com.typesafe.akka::akka-http-core:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-http:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-http-spray-json:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
    ivy"ch.qos.logback:logback-classic:1.2.10",
  )

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
    )
  }
}

object ch03 extends ScalaModule {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
  )

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
    )
  }
}

object ch04 extends ScalaModule {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
  )

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
    )
  }
}

object ch05 extends ScalaModule {
  def scalaVersion = "2.13.6"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.github.nscala-time::nscala-time:2.32.0",
  )

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
    )
  }
}

object ch06 extends ScalaModule {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def akkaHttpVersion = "10.2.10"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
    ivy"com.typesafe.akka::akka-remote:${akkaVersion}",
    ivy"com.typesafe.akka::akka-cluster:${akkaVersion}",
    ivy"com.typesafe.akka::akka-serialization-jackson:${akkaVersion}",
    ivy"com.typesafe.akka::akka-http-core:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-http:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-http-spray-json:${akkaHttpVersion}",
    ivy"ch.qos.logback:logback-classic:1.2.10",
  )

  def mainClass = Some("com.goticks.SingleNodeMain")

  val jarName = "goticks-server.jar"
  def assembly = T {
    val newPath = T.ctx.dest / jarName
    os.move(super.assembly().path, newPath)
    PathRef(newPath)
  }
}

object ch07 extends ScalaModule with plugins.PackageIt {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
			 "-feature", "-language:_") ++ 
		      Seq("-Xlint", "-Ywarn-unused", "-Ywarn-dead-code")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
    ivy"ch.qos.logback:logback-classic:1.2.10",
  )

  override def resources = T.sources {
    super.resources() :+ PathRef(millSourcePath / "universal" / "conf")
  }

  def mainClass = Some("aia.deploy.BootHello")

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
    )
  }
}

object ch08 extends ScalaModule {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
			 "-feature", "-language:_") ++ 
		      Seq("-Xlint", "-Ywarn-unused", "-Ywarn-dead-code")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
  )

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
    )
  }
}

object ch09 extends ScalaModule {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
  			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
  )

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
    )
  }
}

object ch10 extends ScalaModule {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
  			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
  )

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
    )
  }
}

object ch11 extends ScalaModule {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
  			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
  )

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
    )
  }
}

object ch12 extends ScalaModule {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def akkaHttpVersion = "10.2.10"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-stream:${akkaVersion}",
    ivy"com.typesafe.akka::akka-http-core:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-http:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-http-spray-json:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-http-xml:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
    ivy"ch.qos.logback:logback-classic:1.2.10",
  )

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
      ivy"com.typesafe.akka::akka-http-testkit:${akkaHttpVersion}",
    )
  }
}

object ch13 extends ScalaModule {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def akkaHttpVersion = "10.2.10"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-stream:${akkaVersion}",
    ivy"com.typesafe.akka::akka-http-core:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-http:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-http-spray-json:${akkaHttpVersion}",
  )

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
      ivy"com.typesafe.akka::akka-stream-testkit:${akkaVersion}",
    )
  }
}

object ch14 extends ScalaModule with plugins.PackageIt {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
    ivy"com.typesafe.akka::akka-remote:${akkaVersion}",
    ivy"com.typesafe.akka::akka-cluster:${akkaVersion}",
    ivy"com.typesafe.akka::akka-serialization-jackson:${akkaVersion}",
    ivy"ch.qos.logback:logback-classic:1.2.10",
  )

  def mainClass = Some("aia.cluster.words.Main")

  val jarName = "words-node.jar"
  def assembly = T {
    val newPath = T.ctx.dest / jarName
    os.move(super.assembly().path, newPath)
    PathRef(newPath)
  }

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
    )
  }
}

object ch15 extends ScalaModule with plugins.PackageIt {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def akkaHttpVersion = "10.2.10"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
    ivy"com.typesafe.akka::akka-remote:${akkaVersion}",
    ivy"com.typesafe.akka::akka-cluster:${akkaVersion}",
    ivy"com.typesafe.akka::akka-cluster-tools:${akkaVersion}",
    ivy"com.typesafe.akka::akka-cluster-sharding:${akkaVersion}",
    ivy"com.typesafe.akka::akka-serialization-jackson:${akkaVersion}",
    ivy"com.typesafe.akka::akka-persistence:${akkaVersion}",
    ivy"com.typesafe.akka::akka-persistence-query:${akkaVersion}",
    ivy"com.typesafe.akka::akka-http-core:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-http:${akkaHttpVersion}",
    ivy"com.typesafe.akka::akka-http-spray-json:${akkaHttpVersion}",
    ivy"ch.qos.logback:logback-classic:1.2.10",
    ivy"com.fgrutsch::akka-persistence-mapdb:0.3.2",
  )

  def mainClass = Some("aia.persistence.sharded.ShardedMain")

  val jarName = "persistence-examples.jar"
  def assembly = T {
    val newPath = T.ctx.dest / jarName
    os.move(super.assembly().path, newPath)
    PathRef(newPath)
  }

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
      ivy"commons-io:commons-io:2.11.0",
    )
  }
}

object ch16 extends ScalaModule {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
  )

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
    )
  }
}

object ch17 extends ScalaModule {
  def scalaVersion = "2.13.6"
  def akkaVersion = "2.6.20"
  def scalacOptions = Seq("-deprecation", "-unchecked", "-Xfatal-warnings",
			 "-feature", "-language:_")
  def ivyDeps = Agg(
    ivy"com.typesafe.akka::akka-actor:${akkaVersion}",
    ivy"com.typesafe.akka::akka-slf4j:${akkaVersion}",
    ivy"ch.qos.logback:logback-classic:1.2.10",
    ivy"com.typesafe.akka::akka-actor-typed:${akkaVersion}",
    ivy"com.typesafe.akka::akka-persistence:${akkaVersion}",
    ivy"commons-io:commons-io:2.11.0",
  )

  object test extends Tests with TestModule.ScalaTest {
    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.2.14",
      ivy"com.typesafe.akka::akka-testkit:${akkaVersion}",
    )
  }
}
