package com.github.keyno63.caliban

import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps
import scala.io.StdIn
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import caliban.AkkaHttpAdapter
import com.github.keyno63.caliban.api.ExampleApi
import com.github.keyno63.caliban.data.ExampleData.sampleCharacters
import com.github.keyno63.caliban.service.ExampleService
import com.github.keyno63.caliban.service.ExampleService.ExampleService
import sttp.tapir.json.circe._
import zio.internal.Platform
import zio.Runtime
import zio.clock.Clock
import zio.console.Console

object ExampleApp extends App {
  implicit val system: ActorSystem                                      = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor               = system.dispatcher
  implicit val runtime: Runtime[ExampleService with Console with Clock] =
    Runtime.unsafeFromLayer(ExampleService.make(sampleCharacters) ++ Console.live ++ Clock.live, Platform.default)

  val interpreter = runtime.unsafeRun(ExampleApi.api.interpreter)

  /**
   * curl -X POST \
   * http://localhost:8088/api/graphql \
   * -H 'Host: localhost:8088' \
   * -H 'Content-Type: application/json' \
   * -d '{
   * "query": "query { characters { name }}"
   * }'
   */
  val route =
    path("api" / "graphql") {
      AkkaHttpAdapter.makeHttpService(interpreter)
    } ~ path("ws" / "graphql") {
      AkkaHttpAdapter.makeWebSocketService(interpreter)
    } ~ path("graphiql") {
      getFromResource("graphiql.html")
    }

  val bindingFuture = Http()
    .newServerAt("localhost", 8088)
    .bind(route)
  println(s"Server online at http://localhost:8088/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
