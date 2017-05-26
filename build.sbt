name := "scalapill5-cqrs-akka"

version := "1.0"

scalaVersion := "2.12.2"

val akkaVersion = "2.5.2"

scalacOptions := Seq("-encoding", "utf8", "-feature", "-language:postfixOps")

resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.1",
  "org.mockito" % "mockito-all" % "1.10.19",
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.1.1"
)
