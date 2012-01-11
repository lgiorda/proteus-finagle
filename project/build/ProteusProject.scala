import sbt._
import com.twitter.sbt._
import de.element34.sbteclipsify._

class ProteusProject(info: ProjectInfo) extends StandardServiceProject(info)
  with CompileThriftRuby
  with CompileThriftScroogeMixin
  with DefaultRepos
  with Eclipsify
{

  val finagleVersion = "1.9.12"
  val finagleC = "com.twitter" % "finagle-core" % finagleVersion
  val finagleT = "com.twitter" % "finagle-thrift" % finagleVersion
  val finagleO = "com.twitter" % "finagle-ostrich4" % finagleVersion

  // thrift
  val scrooge_runtime = "com.twitter" % "scrooge-runtime" % "1.0.3"
  val libthrift = "thrift" % "libthrift" % "0.5.0"
  val util = "com.twitter" % "util" % "1.11.2"

  // logging
  val slf4jVersion = "1.6.4"
  val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion withSources() intransitive()
  val slf4jBindings = "org.slf4j" % "slf4j-jdk14" % slf4jVersion withSources() intransitive()

  //override def originalThriftNamespaces = Map("Proteus" -> "edu.ciir.proteus.thrift")
  //override val scalaThriftTargetNamespace = "edu.ciir.proteus"

  /**
   * We can change this over to ProteusMain once we've written the class...
   */
  override def mainClass = Some("edu.ciir.searchbird.SearchbirdMain")

}
