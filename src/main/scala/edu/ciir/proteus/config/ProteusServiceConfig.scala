package edu.ciir.proteus.config


import com.twitter.logging.Logger
import com.twitter.logging.config._
import com.twitter.ostrich.admin.{RuntimeEnvironment, ServiceTracker}
import com.twitter.ostrich.admin.config._
import com.twitter.util.Config
import edu.ciir.proteus.thrift._
import edu.ciir.proteus._

class ProteusServiceConfig extends ServerConfig[ProteusNodesService.ThriftServer]{
	var shards: Seq[String] = Seq()
	var manager: String = ""
	var thriftPort: Int = 9999
	var thriftHostname: String = "localhost"
	  
	def apply(runtime: RuntimeEnvironment) = {
    runtime.arguments.get("shard") match {
      case Some(arg) =>
        println("In a shard...")
        val which = arg.toInt
        if (which >= shards.size || which < 0)
          throw new Exception("invalid shard number %d".format(which))

        // override with the shard port
        val Array(_, port) = shards(which).split(":")
        thriftPort = port.toInt

        new Library(this)

      case None =>
        println("In a librarian")
    //    require(!shards.isEmpty)
        //val remotes = shards map { new RemoteIndex(_) }
//        new CompositeNode(remotes)
        val Array(_, port) = manager.split(":")
        thriftPort = port.toInt
        new Librarian(this)
    }
}
}