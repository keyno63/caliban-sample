package com.github.keyno63.caliban.api

import caliban.GraphQL.graphQL
import caliban.{ GraphQL, RootResolver }
import caliban.schema.Annotations.{ GQLDeprecated, GQLDescription }
import caliban.schema.{ GenericSchema, Schema }
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers.{ maxDepth, maxFields, printSlowQueries, timeout }
import com.github.keyno63.caliban.data.ExampleData.{ Character, CharacterArgs, CharactersArgs, Role }
import com.github.keyno63.caliban.service.ExampleService
import com.github.keyno63.caliban.service.ExampleService.ExampleService
import zio.URIO
import zio.clock.Clock
import zio.console.Console
import zio.duration.durationInt
import zio.stream.ZStream

object ExampleApi extends GenericSchema[ExampleService] {

  case class Queries(
    @GQLDescription("Return all characters from a given origin")
    characters: CharactersArgs => URIO[ExampleService, List[Character]],
    @GQLDeprecated("Use `characters`")
    character: CharacterArgs => URIO[ExampleService, Option[Character]]
  )
  case class Mutations(deleteCharacter: CharacterArgs => URIO[ExampleService, Boolean])
  case class Subscriptions(characterDeleted: ZStream[ExampleService, Nothing, String])

  implicit val roleSchema: Schema[Any, Role]                     = Schema.gen
  implicit val characterSchema: Schema[Any, Character]           = Schema.gen
  implicit val characterArgsSchema: Schema[Any, CharacterArgs]   = Schema.gen
  implicit val charactersArgsSchema: Schema[Any, CharactersArgs] = Schema.gen

  val api: GraphQL[Console with Clock with ExampleService] =
    graphQL(
      RootResolver(
        Queries(
          args => ExampleService.getCharacters(args.origin),
          args => ExampleService.findCharacter(args.name)
        ),
        Mutations(args => ExampleService.deleteCharacter(args.name)),
        Subscriptions(ExampleService.deletedEvents)
      )
    ) @@
      maxFields(200) @@               // query analyzer that limit query fields
      maxDepth(30) @@                 // query analyzer that limit query depth
      timeout(3 seconds) @@           // wrapper that fails slow queries
      printSlowQueries(500 millis) @@ // wrapper that logs slow queries
      apolloTracing                   // wrapper for https://github.com/apollographql/apollo-tracing
}
