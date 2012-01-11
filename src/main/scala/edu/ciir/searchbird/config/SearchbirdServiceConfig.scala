package edu.ciir.searchbird
package config

import com.twitter.logging.Logger
import com.twitter.logging.config._
import com.twitter.ostrich.admin.{RuntimeEnvironment, ServiceTracker}
import com.twitter.ostrich.admin.config._
import com.twitter.util.Config
import edu.ciir.proteus.thrift._

class SearchbirdServiceConfig extends ServerConfig[SearchbirdService.ThriftServer]{
	var shards: Seq[String] = Seq()
	var thriftPort: Int = 9999
	
	def apply(runtime: RuntimeEnvironment) = {
    val index = runtime.arguments.get("shard") match {
      case Some(arg) =>
       
        val which = arg.toInt
        if (which >= shards.size || which < 0)
          throw new Exception("invalid shard number %d".format(which))

        // override with the shard port
        val Array(_, port) = shards(which).split(":")
        thriftPort = port.toInt

        new RemoteNode

      case None =>
        require(!shards.isEmpty)
        val remotes = shards map { new RemoteIndex(_) }
        new CompositeNode(remotes)
    }

    new SearchbirdServiceImpl(this, index)
  }
}