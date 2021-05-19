package com.geo.fu

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

import scala.util.{Failure, Success}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
/**
 * @author Gemuruh Geo Pratama
 * @created 19/05/2021-6:49 AM
 */
object Main extends App {
  val log = LoggerFactory.getLogger("Main")
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
  implicit val system = ActorSystem[Nothing](Behaviors.empty, "HelloAkkaHttpServer")
  val config = ConfigFactory.load("application.conf")
  val route = new RouteFileUpload(config).uploadFile
  startHttpServer(route)
}
