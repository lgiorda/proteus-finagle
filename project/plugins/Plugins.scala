import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  lazy val eclipsify = "de.element34" % "sbt-eclipsify" % "0.7.0"
  val twitterRepo = "twitter-repo" at "http://maven.twttr.com/"
  val standardProject = "com.twitter" % "standard-project" % "0.11.16"
  val sbtScrooge = "com.twitter" % "sbt-scrooge" % "1.1.1"
  val sbtThrift = "com.twitter" % "sbt-thrift" % "2.0.2"
}
