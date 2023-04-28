name := "cqs-akka"

version := "0.1"

scalaVersion := "2.13.3"
lazy val akkaVersion = "2.8.0"
lazy val leveldbVersion = "0.12"
lazy val leveldbjniVersion = "1.8"
lazy val postgresVersion = "42.5.4"
lazy val cassandraVersion = "1.1.0"
lazy val json4sVersion = "3.2.11"
lazy val protobufVersion = "3.6.1"
lazy val scalikejdbc = "4.0.0"

scalacOptions += "-deprecation"
resolvers += Resolver.bintrayRepo("akka", "snapshots")

libraryDependencies ++= Seq(

  "org.postgresql" % "postgresql" % postgresVersion,
  "org.scalikejdbc" %% "scalikejdbc" % scalikejdbc,

  "com.typesafe.akka" %% "akka-persistence-cassandra" % cassandraVersion,
  "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % cassandraVersion,

  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,


  //  local levelDB stores
  "org.iq80.leveldb" % "leveldb" % leveldbVersion,
  "org.fusesource.leveldbjni" % "leveldbjni-all" % leveldbjniVersion,

  "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "5.0.0",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,

  "com.typesafe.akka" %% "akka-coordination" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
)

libraryDependencies += "com.typesafe" % "config" % "1.4.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.6"