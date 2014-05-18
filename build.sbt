name := "mongoTest"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "redis.clients" % "jedis" % "2.4.2"
)     

libraryDependencies += "org.mongodb" % "mongo-java-driver" % "2.11.4"

play.Project.playJavaSettings
