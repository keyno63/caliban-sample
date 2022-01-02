name := "caliban-sample"

version := "0.1"

scalaVersion := "2.13.7"
val circeVersion   = "0.13.0"
val calibanVersion = "1.3.1"
val tapirVersion   = "0.19.1"

lazy val common = project.settings(
  libraryDependencies ++= Seq(
    "com.github.ghostdogpr" %% "caliban"
  ).map(_                            % calibanVersion) ++
    Seq(
//      "de.heikoseeberger"            %% "akka-http-circe"               % "1.32.0",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion
    )
)

lazy val akkaSample = project
  .in(file("./akkaSample"))
  .dependsOn(common)
  .settings(
    libraryDependencies ++= Seq(
      "com.github.ghostdogpr" %% "caliban-akka-http"
    ).map(_ % calibanVersion)
  )
