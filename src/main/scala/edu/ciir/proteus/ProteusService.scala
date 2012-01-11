package edu.ciir.proteus

import scala.collection.mutable
import org.apache.thrift.protocol.TBinaryProtocol

import com.twitter.util._
import com.twitter.conversions.time._
import com.twitter.logging.Logger
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import edu.ciir.proteus.thrift._

object Constants {
  val contents_map: Map[ProteusType, Seq[ProteusType]] = Map(ProteusType.Collection -> List(ProteusType.Page), ProteusType.Organization -> List(), ProteusType.Location -> List(), ProteusType.Video -> List(ProteusType.Person, ProteusType.Location, ProteusType.Organization), ProteusType.Page -> List(ProteusType.Picture, ProteusType.Video, ProteusType.Audio), ProteusType.Person -> List(), ProteusType.Picture -> List(ProteusType.Person, ProteusType.Location, ProteusType.Organization), ProteusType.Audio -> List(ProteusType.Person, ProteusType.Location, ProteusType.Organization))
  val container_map: Map[ProteusType, Seq[ProteusType]] = Map(ProteusType.Collection -> List(), ProteusType.Organization -> List(ProteusType.Picture, ProteusType.Video, ProteusType.Audio), ProteusType.Location -> List(ProteusType.Picture, ProteusType.Video, ProteusType.Audio), ProteusType.Video -> List(ProteusType.Page), ProteusType.Page -> List(ProteusType.Collection), ProteusType.Person -> List(ProteusType.Picture, ProteusType.Video, ProteusType.Audio), ProteusType.Picture -> List(ProteusType.Page), ProteusType.Audio -> List(ProteusType.Page))
  val string_map: Map[String, ProteusType] = Map("organization" -> ProteusType.Organization, "video" -> ProteusType.Video, "Page" -> ProteusType.Page, "picture" -> ProteusType.Picture, "collection" -> ProteusType.Collection, "audio" -> ProteusType.Audio, "location" -> ProteusType.Location, "Person" -> ProteusType.Person)
}

trait ProteusService  {

  def getSupportedTypes: Future[List[ProteusType]]
  def getDynamicTransforms: Future[List[DynamicTransformID]]
  def supportsType(ptype: ProteusType): Future[Boolean]
  def supportsDynTranform(dtID: DynamicTransformID): Future[Boolean]
  def runSearch(s: SearchRequest): SearchResponse
  def runContainerTransform(id: AccessIdentifier,
                            from_type: ProteusType,
                            to_type: ProteusType,
                            params: SearchParameters): Future[SearchResponse]
  def runOverlapsTransform(id: AccessIdentifier,
                           from_type: ProteusType,
                           params: SearchParameters): Future[SearchResponse]
  def runOccurAsSubjTransform(id: AccessIdentifier,
                              from_type: ProteusType,
                              term: String,
                              params: SearchParameters): Future[SearchResponse]
  def runOccurAsObjTransform(id: AccessIdentifier,
                             from_type: ProteusType,
                             term: String,
                             params: SearchParameters): Future[SearchResponse]
  def runOccurHasObjTransform(id: AccessIdentifier,
                              from_type: ProteusType,
                              term: String,
                              params: SearchParameters): Future[SearchResponse]
  def runOccurHasSubjTransform(id: AccessIdentifier,
                               from_type: ProteusType,
                               term: String,
                               params: SearchParameters): Future[SearchResponse]
  def runDynamicTransform(id: AccessIdentifier,
                          transform_id: DynamicTransformID,
                          params: SearchParameters): Future[SearchResponse]
  def runNearbyLocationsTransform(id: AccessIdentifier,
                                  radius_miles: Int,
                                  params: SearchParameters): Future[SearchResponse]
  
  def lookupCollection(accessID: AccessIdentifier): Future[Collection]
  def lookupPage(accessID: AccessIdentifier): Future[Page]
  def lookupPicture(accessID: AccessIdentifier): Future[Picture]
  def lookupVideo(accessID: AccessIdentifier): Future[Video]
  def lookupAudio(accessID: AccessIdentifier): Future[Audio]
  def lookupPerson(accessID: AccessIdentifier): Future[Person]
  def lookupLocation(accessID: AccessIdentifier): Future[Location]
  def lookupOrganization(accessID: AccessIdentifier): Future[Organization]

}


abstract class Library extends ProteusService {
  
}

abstract class Librarian extends ProteusService {
  
}