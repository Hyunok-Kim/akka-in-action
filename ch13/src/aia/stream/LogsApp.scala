package aia.stream

import java.nio.file.{Files, FileSystems, Path}
import scala.util.{Success, Failure}
import scala.concurrent.Future
import scala.concurrent.duration._

import akka.NotUsed
import akka.actor.{ActorSystem, Actor, Props}
import akka.event.Logging

import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._

import com.typesafe.config.{Config, ConfigFactory}

object LogsApp extends App {
  
  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  val logsDir = {
    val dir = config.getString("log-stream-processor.logs-dir")
    Files.createDirectories(FileSystems.getDefault.getPath(dir))
  }
  val maxLine = config.getInt("log-stream-processor.max-line")

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher

  val api = new LogsApi(logsDir, maxLine).routes

  val bindingFuture: Future[ServerBinding] =
    Http().newServerAt(host, port).bind(api)

  val log = Logging(system.eventStream, "logs")
  bindingFuture.map { serverBinding =>
    log.info(s"Bound to ${serverBinding.localAddress} ")
  }.onComplete {
    case Success(v) =>
    case Failure(ex) =>
      log.error(ex, "Failed to bind to {}:{}!", host, port)
      system.terminate()
  }
}
