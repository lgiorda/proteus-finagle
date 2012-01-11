package edu.ciir.searchbird

import com.twitter.ostrich.admin.RuntimeEnvironment
import edu.ciir.proteus.thrift._

object SearchbirdMain {
  def main(args: Array[String]) {
    val env = RuntimeEnvironment(this, args)
    val service = env.loadRuntimeConfig[SearchbirdService.ThriftServer]
    println("starting up service...")
    service.start()    
  }
}
