package edu.ciir.proteus

import com.twitter.ostrich.admin.RuntimeEnvironment
import edu.ciir.proteus.thrift._

object ProteusMain {
  def main(args: Array[String]) {
    val env = RuntimeEnvironment(this, args)
    val service = env.loadRuntimeConfig[ProteusNodesService.ThriftServer]
    println("starting up service...")
    service.start()    
  }
}
