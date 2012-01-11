package edu.ciir.searchbird


import java.util.concurrent.Executors
import edu.ciir.proteus.thrift._
import scala.collection.mutable
import com.twitter.util._
import config._

class SearchbirdServiceImpl(config: SearchbirdServiceConfig, searchbirdAbs: SearchbirdService) extends SearchbirdService.ThriftServer {
  val serverName = "Proteus"
  val thriftPort = config.thriftPort

  def get(key: String) = searchbirdAbs.get(key)
  def put(key: String, value: String) =
    searchbirdAbs.put(key, value) map { _ => null: java.lang.Void }
  def search(query: String) = searchbirdAbs.search(query)
}