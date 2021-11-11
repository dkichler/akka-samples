val token = System.getenv("LIGHTBEND_COMMERCIAL_TOKEN")
resolvers in ThisBuild += "lightbend-commercial-mvn" at s"https://repo.lightbend.com/pass/$token/commercial-releases"
resolvers in ThisBuild += Resolver.url("lightbend-commercial-ivy", url(s"https://repo.lightbend.com/pass/$token/commercial-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "0.7.3")
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.4") // ALPN agent
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.25")
addSbtPlugin("com.lightbend.cinnamon"   % "sbt-cinnamon"        % "2.16.2")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.9.0"
