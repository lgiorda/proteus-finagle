package edu.ciir.proteus
import org.apache.thrift.protocol.TBinaryProtocol

import com.twitter.util._
import com.twitter.conversions.time._
import com.twitter.logging.Logger
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import edu.ciir.proteus.thrift._

import java.net.InetSocketAddress

/**
 * Will this be the primary API used by our web framework?
 */
class ProteusClient(libHostName: String, libPort: Int) {
  import Constants._
 
  val transport = ClientBuilder()
    .name("Proteus")
    .hosts(Seq(new InetSocketAddress(libHostName, libPort)))
    .codec(ThriftClientFramedCodec())
    .hostConnectionLimit(1)
    .timeout(5000.milliseconds)
    .build()

  val client = new ProteusNodesService.FinagledClient(transport, new TBinaryProtocol.Factory)
  println("Connected to Librarian at: " + libHostName + ":" + libPort)
  /*** Query & Transform Methods ***/
 
  /**
   * Queries the librarian for the query text (text) over the requested types (types_requested).
   * The result is a Future for a SearchResponse. The results of which can be then looked up to get
   * the full objects.
   */
  def query(text: String, types_requested: List[ProteusType],
            num_requested: Int = 100, start_at: Int = 0, language: String = "en"): Future[SearchResponse] = {

    val search_params = new SearchParameters(Some(num_requested.toShort), Some(start_at.toShort), Some(language))
    val search_request = new SearchRequest(text, types_requested, Some(search_params))
    return client.runSearch(search_request)
  }

  /**
   * A transformation query which gets the contents (reference requests) belonging to
   * a given access identifier (which specifies a data resource). The response is returned as a Future.
   */
  def getContents(id: AccessIdentifier, id_type: ProteusType, contents_type: ProteusType,
                  num_requested: Int = 100, start_at: Int = 0, language: String = "en"): Future[SearchResponse] = {

    if (!contents_map(id_type).contains(contents_type))
      throw new IllegalArgumentException("Mismatched to/from types for getContents: (" + id_type + ", " + contents_type + ")")

    val search_params = new SearchParameters(Some(num_requested.toShort), Some(start_at.toShort), Some(language))
    return client.runContentsTransform(id, id_type, contents_type, search_params)
  }

  /**
   * Get the reference result for the containing data resource of of this access identifier
   */
  def getContainer(id: AccessIdentifier, id_type: ProteusType, container_type: ProteusType,
                   num_requested: Int = 100, start_at: Int = 0, language: String = "en"): Future[SearchResponse] = {

    if (!container_map(id_type).contains(container_type))
      throw new IllegalArgumentException("Mismatched to/from types for getContainer: (" + id_type + ", " + container_type + ")")

    val search_params = new SearchParameters(Some(num_requested.toShort), Some(start_at.toShort), Some(language))
    return client.runContainerTransform(id, id_type, container_type, search_params)

  }

  private def recurseHierarchy(result: SearchResult, type_path: List[ProteusType],
                               num_requested: Int, language: String, ascend: Boolean = false): Future[List[SearchResult]] = {

    if (type_path.length == 1)
      return Future { List(result) }
    else {
      val converted = if (!ascend) getContents(result.id, type_path(0), type_path(1), num_requested = num_requested, language = language)
      else getContainer(result.id, type_path(0), type_path(1), num_requested = num_requested, language = language)

      val recursed = converted flatMap {
        next_results =>
          val result_list = next_results.results.map(result => recurseHierarchy(result, type_path.drop(1), scala.math.ceil(num_requested.toDouble / next_results.results.length).toInt, language, ascend = ascend))
          Future.collect(result_list)
      }
      return recursed map { _.flatten.toList }
    }
  }

  /**
   * A transformation query which gets the contents (reference requests) belonging to
   * a given access identifier (which specifies a data resource). The response is returned as a Future.
   */
  def getDescendants(start_item: SearchResult, type_path: List[ProteusType],
                     num_requested: Int = 100, language: String = "en"): Future[List[SearchResult]] = {

    // Verify that the type_path is a valid path
    if (!type_path.dropRight(1).zip(type_path.drop(1)).forall(t => contents_map(t._1).contains(t._2)))
      throw new IllegalArgumentException("Mismatched type path for getDescendants: (" + type_path.mkString(", ") + ")")

    // Recursively go through and getContents, until we reach the end...
    val results = recurseHierarchy(start_item, type_path, num_requested, language)
    return results
  }

  /**
   * Get the reference result for the containing data resource of of this access identifier
   */
  def getAncesters(start_item: SearchResult, type_path: List[ProteusType],
                   num_requested: Int = 100, language: String = "en"): Future[List[SearchResult]] = {
    // Verify that the type_path is a valid path
    if (!type_path.dropRight(1).zip(type_path.drop(1)).forall(t => container_map(t._1).contains(t._2)))
      throw new IllegalArgumentException("Mismatched type path for getAncesters: (" + type_path.mkString(", ") + ")")

    // Recursively go through and getContainer, until we reach the end...
    val results = recurseHierarchy(start_item, type_path, num_requested, language, ascend = true)
    return results
  }

  /**
   * Get the overlaping resources of the same type as this one. Where the precise meaning of overlapping
   * is up to the end point data stores to decide.
   */
  def getOverlaps(id: AccessIdentifier, id_type: ProteusType, num_requested: Int = 100, start_at: Int = 0, language: String = "en"): Future[SearchResponse] = {
    val search_params = new SearchParameters(Some(num_requested.toShort), Some(start_at.toShort), Some(language))
    return client.runOverlapsTransform(id, id_type, search_params)
  }

  /**
   * Get the references to Pages where this person, location, or organization identified by id,
   * occurs as an object of the provided term.
   */
  def getOccurrencesAsObj(id: AccessIdentifier, id_type: ProteusType, term: String,
                          num_requested: Int = 100, start_at: Int = 0, language: String = "en"): Future[SearchResponse] = {

    val search_params = new SearchParameters(Some(num_requested.toShort), Some(start_at.toShort), Some(language))
    return client.runOccurAsObjTransform(id, id_type, term, search_params)
  }

  /**
   * Get the references to Pages where this person, location, or organization identified by id,
   * occurs as a subject of the provided term.
   */
  def getOccurencesAsSubj(id: AccessIdentifier, id_type: ProteusType, term: String,
                          num_requested: Int = 100, start_at: Int = 0, language: String = "en"): Future[SearchResponse] = {

    val search_params = new SearchParameters(Some(num_requested.toShort), Some(start_at.toShort), Some(language))
    return client.runOccurAsSubjTransform(id, id_type, term, search_params)
  }

  /**
   * Get the references to Pages where this person, location, or organization identified by id,
   * occurs having as its object the provided term.
   */
  def getOccurrencesHasObj(id: AccessIdentifier, id_type: ProteusType, term: String,
                           num_requested: Int = 100, start_at: Int = 0, language: String = "en"): Future[SearchResponse] = {

    val search_params = new SearchParameters(Some(num_requested.toShort), Some(start_at.toShort), Some(language))
    return client.runOccurHasObjTransform(id, id_type, term, search_params)
  }

  /**
   * Get the references to Pages where this person, location, or organization identified by id,
   * occurs having as its subject the provided term.
   */
  def getOccurrencesHasSubj(id: AccessIdentifier, id_type: ProteusType, term: String,
                            num_requested: Int = 100, start_at: Int = 0, language: String = "en"): Future[SearchResponse] = {

    val search_params = new SearchParameters(Some(num_requested.toShort), Some(start_at.toShort), Some(language))
    return client.runOccurHasSubjTransform(id, id_type, term, search_params)
  }

  /**
   * Get locations within radius of the location described by id
   */
  def getNearbyLocations(id: AccessIdentifier, radius: Int,
                         num_requested: Int = 100, start_at: Int = 0, language: String = "en"): Future[SearchResponse] = {
    val search_params = new SearchParameters(Some(num_requested.toShort), Some(start_at.toShort), Some(language))
    return client.runNearbyLocationsTransform(id, radius, search_params)
  }

  /**
   * Use a dynamically loaded transform from id (of corresponding type id_type), where the name of the transform is
   * transform_name. The librarian must have a end point supporting this transform loaded for this to succeed.
   */
  def useDynamicTransform(id: AccessIdentifier, id_type: ProteusType, transform_name: String,
                          num_requested: Int = 100, start_at: Int = 0, language: String = "en"): Future[SearchResponse] = {
    val search_params = new SearchParameters(Some(num_requested.toShort), Some(start_at.toShort), Some(language))
    val dynTransID = new DynamicTransformID(transform_name, Some(id_type))
    return client.runDynamicTransform(id, dynTransID, search_params)

  }

  /*** Lookup Methods ***/

  /**
   * Request that the librarian look up a Collection by its reference (SearchResult) and return
   * a Future to the full object.
   */
  def lookupCollection(result: SearchResult): Future[Collection] = {
    // Sanity checking first
    if (result.proteusType.get != ProteusType.Collection)
      throw new IllegalArgumentException("Mismatched type with lookup method")
    return client.lookupCollection(result.id)
  }

  /**
   * Request the librarian look up a Page by its result reference
   */
  def lookupPage(result: SearchResult): Future[Page] = {
    // Sanity checking first
    if (result.proteusType.get != ProteusType.Page)
      throw new IllegalArgumentException("Mismatched type with lookup method")

    return client.lookupPage(result.id)
  }

  /**
   * Request the librarian look up a Picture by its result reference
   */
  def lookupPicture(result: SearchResult): Future[Picture] = {
    // Sanity checking first
    if (result.proteusType.get != ProteusType.Picture)
      throw new IllegalArgumentException("Mismatched type with lookup method")

    return client.lookupPicture(result.id)
  }

  /**
   * Request the librarian look up a Video by its result reference
   */
  def lookupVideo(result: SearchResult): Future[Video] = {
    // Sanity checking first
    if (result.proteusType.get != ProteusType.Video)
      throw new IllegalArgumentException("Mismatched type with lookup method")

    return client.lookupVideo(result.id)
  }

  /**
   * Request the librarian look up a Audio clip by its result reference
   */
  def lookupAudio(result: SearchResult): Future[Audio] = {
    // Sanity checking first
    if (result.proteusType.get != ProteusType.Audio)
      throw new IllegalArgumentException("Mismatched type with lookup method")

    return client.lookupAudio(result.id)
  }

  /**
   * Request the librarian look up a Person by its result reference
   */
  def lookupPerson(result: SearchResult): Future[Person] = {
    // Sanity checking first
    if (result.proteusType.get != ProteusType.Person)
      throw new IllegalArgumentException("Mismatched type with lookup method")

    return client.lookupPerson(result.id)
  }

  /**
   * Request the librarian look up a Location by its result reference
   */
  def lookupLocation(result: SearchResult): Future[Location] = {
    println("Looking up location..")
    // Sanity checking first
    if (result.proteusType.get != ProteusType.Location)
      throw new IllegalArgumentException("Mismatched type with lookup method")

    return client.lookupLocation(result.id)
  }

  /**
   * Request the librarian look up a Page by its result reference
   */
  def lookupOrganization(result: SearchResult): Future[Organization] = {
    // Sanity checking first
    if (result.proteusType.get != ProteusType.Organization)
      throw new IllegalArgumentException("Mismatched type with lookup method")

    return client.lookupOrganization(result.id)
  }

  /** Utility functions to make interacting with the data easier **/

  /**
   * Take a ResultSummary and turn it into a string with html tags around the
   * highlighted text regions.
   */
  def tagTerms(summary: ResultSummary, startTag: String = "<b>", endTag: String = "</b>") = wrapTerms(summary.text, summary.highlights.toList, startTag = startTag, endTag = endTag)

  protected def wrapTerms(description: String, locations: List[TextRegion], startTag: String, endTag: String): String = {
    if (locations.length == 0)
      return ""
    else if (locations.length == 1)
      return startTag + description.slice(locations(0).start, locations(0).stop) + endTag +
        description.slice(locations(0).stop, description.length)
    else
      return startTag + description.slice(locations(0).start, locations(0).stop) + endTag +
        description.slice(locations(0).stop, locations(1).start) + wrapTerms(description, locations.drop(1), startTag, endTag)
  }

}