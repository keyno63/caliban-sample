name := "caliban-sample"

version := "0.1"

scalaVersion := "2.13.2"
val circeVersion = "0.13.0"
val calibanVersion = "1.0.1"

lazy val common = Seq(
  libraryDependencies ++= Seq(
    "com.github.ghostdogpr" %% "caliban"
  ).map(_ % calibanVersion) ++
    Seq(
      "de.heikoseeberger"            %% "akka-http-circe"               % "1.32.0",
      "io.circe"                     %% "circe-generic"                 % circeVersion
    )
)
lazy val akkaSample = project
  .in(file("./akkaSample"))
  .settings(
    libraryDependencies ++= Seq(
      "com.github.ghostdogpr" %% "caliban-akka-http"
    ).map(_ % calibanVersion) ++ Seq(
      "de.heikoseeberger"            %% "akka-http-circe"               % "1.32.0",
    )
  )