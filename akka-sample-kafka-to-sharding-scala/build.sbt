import com.lightbend.cinnamon.sbt.Cinnamon

val token = System.getenv("LIGHTBEND_COMMERCIAL_TOKEN")
resolvers in ThisBuild += "lightbend-commercial-mvn" at s"https://repo.lightbend.com/pass/$token/commercial-releases"
resolvers in ThisBuild += Resolver.url("lightbend-commercial-ivy", url(s"https://repo.lightbend.com/pass/$token/commercial-releases"))(Resolver.ivyStylePatterns)

val AkkaVersion = "2.6.17"
val AlpakkaKafkaVersion = "2.0.4"
val AkkaManagementVersion = "1.0.5"
val AkkaHttpVersion = "10.1.11"
val EmbeddedKafkaVersion = "2.4.1.1"
val LogbackVersion = "1.2.3"
val CinnamonVersion = "2.16.2"

ThisBuild / scalaVersion := "2.13.5"
ThisBuild / organization := "com.lightbend.akka.samples"
ThisBuild / scalacOptions in Compile ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlog-reflective-calls",
  "-Xlint")
ThisBuild / javacOptions in Compile ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")
ThisBuild / testOptions in Test += Tests.Argument("-oDF")
ThisBuild / licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0")))
ThisBuild / resolvers ++= Seq(
  "Akka Snapshots" at "https://repo.akka.io/snapshots",
  Resolver.bintrayRepo("akka", "snapshots")
)



Global / cancelable := true // ctrl-c
ThisBuild / run / cinnamon := true

lazy val `akka-sample-kafka-to-sharding` = project.in(file(".")).aggregate(producer, processor, client)

lazy val exposeJmx = Seq("-Dcom.sun.management.jmxremote.port=9999",
  "-Dcom.sun.management.jmxremote.authenticate=false",
  "-Dcom.sun.management.jmxremote.ssl=false")
lazy val kafka = project
  .in(file("kafka"))
  .settings(
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "org.slf4j" % "log4j-over-slf4j" % "1.7.26",
      "io.github.embeddedkafka" %% "embedded-kafka" % EmbeddedKafkaVersion),
    cancelable := false)

lazy val client = project
  .in(file("client"))
  .enablePlugins(AkkaGrpcPlugin, JavaAgent)
  .settings(
    libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
        "com.typesafe.akka" %% "akka-discovery" % AkkaVersion))


lazy val processor = project
  .in(file("processor"))
  .enablePlugins(AkkaGrpcPlugin, JavaAgent, Cinnamon)
  .settings(javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.9" % "runtime;test")
  .settings(
    javaOptions ++= exposeJmx,
    run / cinnamon := true,
    cinnamonLogLevel := "INFO",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream-kafka" % AlpakkaKafkaVersion,
      "com.typesafe.akka" %% "akka-stream-kafka-cluster-sharding" % AlpakkaKafkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
      "com.lightbend.akka.management" %% "akka-management" % AkkaManagementVersion,
      "com.lightbend.akka.management" %% "akka-management-cluster-http" % AkkaManagementVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      Cinnamon.library.cinnamonAkkaTyped,
      Cinnamon.library.cinnamonPrometheus,
      // lets it export all metrics
      Cinnamon.library.cinnamonPrometheusHttpServer,
      Cinnamon.library.cinnamonJmxImporter,
      // Disabling this dependency to avoid pulling in the full reference.conf, and demonstrate the issue with only a subset of configuration in cinnamon.conf
      //Cinnamon.library.cinnamonKafkaConsumerJmxImporter, // exports Kafka consumer metrics
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.8" % Test))

lazy val producer = project
  .in(file("producer"))
  .settings(PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value))
  .settings(
    javaOptions ++= exposeJmx,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream-kafka" % AlpakkaKafkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest" %% "scalatest" % "3.0.8" % Test
    )
  )
