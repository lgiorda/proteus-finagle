package edu.ciir.proteus


import scala.collection.mutable
import org.apache.thrift.protocol.TBinaryProtocol

import com.twitter.util._
import com.twitter.conversions.time._
import com.twitter.logging.Logger
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import edu.ciir.proteus.thrift._

trait LibrarianQueryHandler {
  def typeSupport(ptypes: List[ProteusType], groupId: String = "") : List[String]
  def dynamicSupport(dynID: DynamicTransformID, groupId: String = "") : List[String]
  def getGroupId(accessID: AccessIdentifier) : String
  
  def libraries: collection.mutable.HashMap[String, RemoteLibrary]
  
  // TODO: adjust all of these so they actually obey the num_requested and start_at parameters
  
  def runSearch(s: SearchRequest): Future[SearchResponse] = {
    val members = typeSupport(s.types.toList).map(m => libraries(m).runSearch(s) map {sresp => sresp.results })
    return Future.collect(members) map { allresults => new SearchResponse(allresults.flatten.toList) }
  }
 
  def runContainerTransform(id: AccessIdentifier,
                            from_type: ProteusType,
                            to_type: ProteusType,
                            params: SearchParameters): Future[SearchResponse] = {
    val members = typeSupport(List(from_type), getGroupId(id)).map(m => libraries(m).runContainerTransform(id, from_type, to_type,params) map {sresp => sresp.results })
    return Future.collect(members) map { allresults => new SearchResponse(allresults.flatten.toList) }
  }

  def runContentsTransform(id: AccessIdentifier,
                            from_type: ProteusType,
                            to_type: ProteusType,
                            params: SearchParameters): Future[SearchResponse] = {
    val members = typeSupport(List(to_type), getGroupId(id)).map(m => libraries(m).runContentsTransform(id, from_type, to_type,params) map {sresp => sresp.results })
    return Future.collect(members) map { allresults => new SearchResponse(allresults.flatten.toList) }
  }
                            
  def runOverlapsTransform(id: AccessIdentifier,
                           from_type: ProteusType,
                           params: SearchParameters): Future[SearchResponse] = {
    val members = typeSupport(List(from_type), getGroupId(id)).map(m => libraries(m).runOverlapsTransform(id, from_type, params) map {sresp => sresp.results })
    return Future.collect(members) map { allresults => new SearchResponse(allresults.flatten.toList) }
  }
  
  def runOccurAsSubjTransform(id: AccessIdentifier,
                              from_type: ProteusType,
                              term: String,
                              params: SearchParameters): Future[SearchResponse] = {
    val members = typeSupport(List(from_type), getGroupId(id)).map(m => libraries(m).runOccurAsSubjTransform(id, from_type, term, params) map {sresp => sresp.results })
    return Future.collect(members) map { allresults => new SearchResponse(allresults.flatten.toList) }
  }
  
  def runOccurAsObjTransform(id: AccessIdentifier,
                             from_type: ProteusType,
                             term: String,
                             params: SearchParameters): Future[SearchResponse] = {
    val members = typeSupport(List(from_type), getGroupId(id)).map(m => libraries(m).runOccurAsObjTransform(id, from_type, term, params) map {sresp => sresp.results })
    return Future.collect(members) map { allresults => new SearchResponse(allresults.flatten.toList) }
  }
  
  def runOccurHasObjTransform(id: AccessIdentifier,
                              from_type: ProteusType,
                              term: String,
                              params: SearchParameters): Future[SearchResponse] = {
    val members = typeSupport(List(from_type), getGroupId(id)).map(m => libraries(m).runOccurHasObjTransform(id, from_type, term, params) map {sresp => sresp.results })
    return Future.collect(members) map { allresults => new SearchResponse(allresults.flatten.toList) }
  }
  
  def runOccurHasSubjTransform(id: AccessIdentifier,
                               from_type: ProteusType,
                               term: String,
                               params: SearchParameters): Future[SearchResponse] = {
    val members = typeSupport(List(from_type), getGroupId(id)).map(m => libraries(m).runOccurHasSubjTransform(id, from_type, term, params) map {sresp => sresp.results })
    return Future.collect(members) map { allresults => new SearchResponse(allresults.flatten.toList) }
  }
  
  def runDynamicTransform(id: AccessIdentifier,
                          transform_id: DynamicTransformID,
                          params: SearchParameters): Future[SearchResponse] = {
    val members = dynamicSupport(transform_id, getGroupId(id)).map(m => libraries(m).runDynamicTransform(id, transform_id, params) map {sresp => sresp.results })
    return Future.collect(members) map { allresults => new SearchResponse(allresults.flatten.toList) }
  }
  
  
  def runNearbyLocationsTransform(id: AccessIdentifier,
                                  radius_miles: Int,
                                  params: SearchParameters): Future[SearchResponse] = {
    val members = typeSupport(List(ProteusType.Location), getGroupId(id)).map(m => libraries(m).runNearbyLocationsTransform(id, radius_miles, params) map {sresp => sresp.results })
    return Future.collect(members) map { allresults => new SearchResponse(allresults.flatten.toList) }
  }
  
}

trait LibrarianLookupManager {
  def libraries: collection.mutable.HashMap[String, RemoteLibrary]
  
  def lookupCollection(accessID: AccessIdentifier): Future[Collection] = libraries(accessID.resourceId.get).lookupCollection(accessID)
  def lookupPage(accessID: AccessIdentifier): Future[Page] = libraries(accessID.resourceId.get).lookupPage(accessID)
  def lookupPicture(accessID: AccessIdentifier): Future[Picture] = libraries(accessID.resourceId.get).lookupPicture(accessID)
  def lookupVideo(accessID: AccessIdentifier): Future[Video] = libraries(accessID.resourceId.get).lookupVideo(accessID)
  def lookupAudio(accessID: AccessIdentifier): Future[Audio] = libraries(accessID.resourceId.get).lookupAudio(accessID)
  def lookupPerson(accessID: AccessIdentifier): Future[Person] = libraries(accessID.resourceId.get).lookupPerson(accessID)
  def lookupLocation(accessID: AccessIdentifier): Future[Location] = libraries(accessID.resourceId.get).lookupLocation(accessID)
  def lookupOrganization(accessID: AccessIdentifier): Future[Organization] = libraries(accessID.resourceId.get).lookupOrganization(accessID)  
}


trait LibrarianConnectionManager extends RandomDataGenerator {
  
    val libraries = new collection.mutable.HashMap[String, RemoteLibrary]()
  	// Libraries with the same groupID can support each other and combine to form a single resource
  	val group_membership = new collection.mutable.HashMap[String, List[String]]()
  	
  	var supported_types = Set[ProteusType]()
  	var supported_dyn_transforms = List[DynamicTransformID]()
  	
  	 def libKeyID(c: ConnectLibrary) : String = {
      
        val reqKey = c.requestedKey.getOrElse(genKey())
        if (libraries.contains(reqKey)) {
          val collision_conn = libraries(reqKey)
          if (collision_conn.hostname == c.hostname && 
              collision_conn.port == c.port.get && 
              collision_conn.groupId == c.groupId.get)
            return reqKey
          else
        	return ""
        } else 
          return reqKey
    }
    
  	def connectLibrary(details: ConnectLibrary): Future[LibraryConnected] = {
  		val id = libKeyID(details)
  		if (id == "") return Future { new LibraryConnected(details.requestedKey, Some("Requested ID key is already in use by another host.")) }
  		
  		val gID: String = details.groupId.getOrElse(genKey())
  		if (group_membership.contains(gID))
  		  group_membership(gID) ::= id
  		else
  		  group_membership += gID -> List(id)

  		val new_details = new ConnectLibrary(details.hostname, details.port, Some(gID), Some(id), details.supportedTypes, details.dynamicTransforms)
  		libraries += id -> new RemoteLibrary(new_details)
  		
  		return Future { new LibraryConnected(resourceId = Option(id)) }
    }
  	
  	def getSupportedTypes: Future[List[ProteusType]] = Future.collect(libraries.values.map(_.getSupportedTypes).toList) map { s => (Set() ++ s.flatten).toList }
    def getDynamicTransforms: Future[List[DynamicTransformID]] = Future.collect(libraries.values.map(_.getDynamicTransforms).toList) map { s => (Set() ++ s.flatten).toList }
    def supportsType(ptype: ProteusType): Future[Boolean] = Future.collect(libraries.values.map(_.supportsType(ptype)).toList) map { _.contains(true) }
    def supportsDynTransform(dtID: DynamicTransformID): Future[Boolean] = Future.collect(libraries.values.map(_.supportsDynTransform(dtID)).toList) map { _.contains(true) }
    
     def typeSupport(ptypes: List[ProteusType], groupId: String = "") : List[String] = {
      val type_filtered = libraries.keys.toList.filter(k => (libraries(k).getSupportedTypes map { tps: List[ProteusType] => tps.exists(t => ptypes.contains(t)) }).get)
      if(groupId == "") return type_filtered
      else return type_filtered.filter(f => group_membership(groupId).contains(f))
    }
    
	 def dynamicSupport(dynID: DynamicTransformID, groupId: String = "") : List[String] = {
	  return libraries.values.toList.filter(lib => (groupId == "" || lib.groupId == groupId) && lib.supportsDynTransform(dynID).get).map(_.id)
	}
	
	 def getGroupId(accessID: AccessIdentifier) : String = if(libraries.contains(accessID.resourceId.getOrElse(""))) libraries(accessID.resourceId.getOrElse("")).groupId else ""
  
}

class RemoteLibrary(details: ConnectLibrary) extends ProteusService {
  val hostname = details.hostname
  val port = details.port.get
  val groupId = details.groupId.get
  val id = details.requestedKey.get
  
  val transport = ClientBuilder()
    .name("remoteIndex")
    .hosts(details.hostname + ":" + details.port)
    .codec(ThriftClientFramedCodec())
    .hostConnectionLimit(1)
    .timeout(500.milliseconds)
    .build()

  val client = new ProteusNodesService.FinagledClient(transport, new TBinaryProtocol.Factory)

  def getSupportedTypes: Future[List[ProteusType]] = Future { details.supportedTypes.toList }
  def getDynamicTransforms: Future[List[DynamicTransformID]] = Future { details.dynamicTransforms.toList }
  def supportsType(ptype: ProteusType): Future[Boolean] = Future { details.supportedTypes.contains(ptype) }
  def supportsDynTransform(dtID: DynamicTransformID): Future[Boolean] = Future { details.dynamicTransforms.contains(dtID) }
  
  def runSearch(s: SearchRequest): Future[SearchResponse] = client.runSearch(s)
  def runContainerTransform(id: AccessIdentifier,
                            from_type: ProteusType,
                            to_type: ProteusType,
                            params: SearchParameters): Future[SearchResponse] = client.runContainerTransform(id, from_type, to_type,params)

  def runContentsTransform(id: AccessIdentifier,
                            from_type: ProteusType,
                            to_type: ProteusType,
                            params: SearchParameters): Future[SearchResponse] = client.runContentsTransform(id, from_type, to_type,params)
                            
  def runOverlapsTransform(id: AccessIdentifier,
                           from_type: ProteusType,
                           params: SearchParameters): Future[SearchResponse] = client.runOverlapsTransform(id, from_type, params)
  def runOccurAsSubjTransform(id: AccessIdentifier,
                              from_type: ProteusType,
                              term: String,
                              params: SearchParameters): Future[SearchResponse] = client.runOccurAsSubjTransform(id, from_type, term, params)
  def runOccurAsObjTransform(id: AccessIdentifier,
                             from_type: ProteusType,
                             term: String,
                             params: SearchParameters): Future[SearchResponse] = client.runOccurAsObjTransform(id, from_type, term, params)
  def runOccurHasObjTransform(id: AccessIdentifier,
                              from_type: ProteusType,
                              term: String,
                              params: SearchParameters): Future[SearchResponse] = client.runOccurHasObjTransform(id, from_type, term, params)
  def runOccurHasSubjTransform(id: AccessIdentifier,
                               from_type: ProteusType,
                               term: String,
                               params: SearchParameters): Future[SearchResponse] = client.runOccurHasSubjTransform(id, from_type, term, params)
  def runDynamicTransform(id: AccessIdentifier,
                          transform_id: DynamicTransformID,
                          params: SearchParameters): Future[SearchResponse] = client.runDynamicTransform(id, transform_id, params)
  def runNearbyLocationsTransform(id: AccessIdentifier,
                                  radius_miles: Int,
                                  params: SearchParameters): Future[SearchResponse] = client.runNearbyLocationsTransform(id, radius_miles, params)
  
  def lookupCollection(accessID: AccessIdentifier): Future[Collection] = client.lookupCollection(accessID)
  def lookupPage(accessID: AccessIdentifier): Future[Page] = client.lookupPage(accessID)
  def lookupPicture(accessID: AccessIdentifier): Future[Picture] = client.lookupPicture(accessID)
  def lookupVideo(accessID: AccessIdentifier): Future[Video] = client.lookupVideo(accessID)
  def lookupAudio(accessID: AccessIdentifier): Future[Audio] = client.lookupAudio(accessID)
  def lookupPerson(accessID: AccessIdentifier): Future[Person] = client.lookupPerson(accessID)
  def lookupLocation(accessID: AccessIdentifier): Future[Location] = client.lookupLocation(accessID)
  def lookupOrganization(accessID: AccessIdentifier): Future[Organization] = client.lookupOrganization(accessID)
  
}