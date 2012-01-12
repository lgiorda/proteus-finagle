package edu.ciir.proteus

import scala.collection.mutable
import org.apache.thrift.protocol.TBinaryProtocol
import java.net.InetSocketAddress
import com.twitter.util._
import com.twitter.conversions.time._
import com.twitter.logging.Logger
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import edu.ciir.proteus.thrift._
import java.util.concurrent.Executors
import edu.ciir.proteus.thrift._
import scala.collection.mutable
import com.twitter.util._
import config._

object Constants {
  val contents_map: Map[ProteusType, Seq[ProteusType]] = Map(ProteusType.Collection -> List(ProteusType.Page), ProteusType.Organization -> List(), ProteusType.Location -> List(), ProteusType.Video -> List(ProteusType.Person, ProteusType.Location, ProteusType.Organization), ProteusType.Page -> List(ProteusType.Picture, ProteusType.Video, ProteusType.Audio), ProteusType.Person -> List(), ProteusType.Picture -> List(ProteusType.Person, ProteusType.Location, ProteusType.Organization), ProteusType.Audio -> List(ProteusType.Person, ProteusType.Location, ProteusType.Organization))
  val container_map: Map[ProteusType, Seq[ProteusType]] = Map(ProteusType.Collection -> List(), ProteusType.Organization -> List(ProteusType.Picture, ProteusType.Video, ProteusType.Audio), ProteusType.Location -> List(ProteusType.Picture, ProteusType.Video, ProteusType.Audio), ProteusType.Video -> List(ProteusType.Page), ProteusType.Page -> List(ProteusType.Collection), ProteusType.Person -> List(ProteusType.Picture, ProteusType.Video, ProteusType.Audio), ProteusType.Picture -> List(ProteusType.Page), ProteusType.Audio -> List(ProteusType.Page))
  val string_map: Map[String, ProteusType] = Map("organization" -> ProteusType.Organization, "video" -> ProteusType.Video, "Page" -> ProteusType.Page, "picture" -> ProteusType.Picture, "collection" -> ProteusType.Collection, "audio" -> ProteusType.Audio, "location" -> ProteusType.Location, "Person" -> ProteusType.Person)
}

trait RandomDataGenerator {
  val keyChars: String = (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')).mkString("")
  /**
   * Generates a random alpha-numeric string of fixed length (8).
   * Granted, this is a BAD way to do it, because it doesn't guarantee true randomness.
   */
  def genKey(length: Int = 8): String = (1 to length).map(x => keyChars.charAt(util.Random.nextInt(keyChars.length))).mkString

}

trait RandomEndPoint extends RandomDataGenerator {

  val imgURLs = List[String]("http://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Abraham_Lincoln_head_on_shoulders_photo_portrait.jpg/220px-Abraham_Lincoln_head_on_shoulders_photo_portrait.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/3/39/GodfreyKneller-IsaacNewton-1689.jpg/225px-GodfreyKneller-IsaacNewton-1689.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Thomas_Bayes.gif/300px-Thomas_Bayes.gif",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Johannes_Kepler_1610.jpg/225px-Johannes_Kepler_1610.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Einstein_1921_portrait2.jpg/225px-Einstein_1921_portrait2.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Justus_Sustermans_-_Portrait_of_Galileo_Galilei%2C_1636.jpg/225px-Justus_Sustermans_-_Portrait_of_Galileo_Galilei%2C_1636.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/Thomas_Jefferson_by_Rembrandt_Peale%2C_1800.jpg/220px-Thomas_Jefferson_by_Rembrandt_Peale%2C_1800.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/BenFranklinDuplessis.jpg/220px-BenFranklinDuplessis.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/EU-Austria.svg/250px-EU-Austria.svg.png",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Clock_Tower_-_Palace_of_Westminster%2C_London_-_September_2006-2.jpg/220px-Clock_Tower_-_Palace_of_Westminster%2C_London_-_September_2006-2.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Grand_canyon_hermits_rest_2010.JPG/250px-Grand_canyon_hermits_rest_2010.JPG",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/Great_Wall_of_China_July_2006.JPG/248px-Great_Wall_of_China_July_2006.JPG")

  val thumbURLs = List[String]("http://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Abraham_Lincoln_head_on_shoulders_photo_portrait.jpg/220px-Abraham_Lincoln_head_on_shoulders_photo_portrait.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/3/39/GodfreyKneller-IsaacNewton-1689.jpg/225px-GodfreyKneller-IsaacNewton-1689.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Thomas_Bayes.gif/300px-Thomas_Bayes.gif",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Johannes_Kepler_1610.jpg/225px-Johannes_Kepler_1610.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/f/f5/Einstein_1921_portrait2.jpg/225px-Einstein_1921_portrait2.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Justus_Sustermans_-_Portrait_of_Galileo_Galilei%2C_1636.jpg/225px-Justus_Sustermans_-_Portrait_of_Galileo_Galilei%2C_1636.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/Thomas_Jefferson_by_Rembrandt_Peale%2C_1800.jpg/220px-Thomas_Jefferson_by_Rembrandt_Peale%2C_1800.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/BenFranklinDuplessis.jpg/220px-BenFranklinDuplessis.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/9/9d/EU-Austria.svg/250px-EU-Austria.svg.png",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Clock_Tower_-_Palace_of_Westminster%2C_London_-_September_2006-2.jpg/220px-Clock_Tower_-_Palace_of_Westminster%2C_London_-_September_2006-2.jpg",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/a/a0/Grand_canyon_hermits_rest_2010.JPG/250px-Grand_canyon_hermits_rest_2010.JPG",
    "http://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/Great_Wall_of_China_July_2006.JPG/248px-Great_Wall_of_China_July_2006.JPG")

  val wikiLinks = List[String]("http://en.wikipedia.org/wiki/University_of_Massachusetts_Amherst",
    "http://en.wikipedia.org/wiki/Bono",
    "http://en.wikipedia.org/wiki/One_Laptop_per_Child")

  val extLinks = List[String]("http://www.archive.org/stream/surveypart5ofconditiounitrich",
    "http://www.archive.org/stream/historyofoneidac11cook",
    "http://www.archive.org/stream/completeguidetoh00foxduoft",
    "http://www.youtube.com/watch?v=4M98x-FLp7E",
    "http://www.youtube.com/watch?v=qYx7YG0RsFY",
    "http://www.youtube.com/watch?v=W1czBcnX1Ww",
    "http://www.youtube.com/watch?v=dQw4w9WgXcQ")

  /**Methods defined elsewhere... ? **/
  //  def containerFor(ptype: ProteusType): List[ProteusType]
  def getResourceKey: String
  //  def numProteusTypes: Int

  def getSupportedTypes = Future { List(ProteusType.apply(1)) }

  def getDynamicTransforms = {
    val firstDTID = new DynamicTransformID("weird", Some(ProteusType.Collection))
    val secondDTID = new DynamicTransformID("illustrative", Some(ProteusType.Person))
    val thirdDTID = new DynamicTransformID("weird", Some(ProteusType.Page))
    Future { List(firstDTID, secondDTID, thirdDTID) }
  }
  def supportsType(ptype: ProteusType) = Future(true)

  /** Internal Utility Methods **/

  def generateRandomSummary: ResultSummary = {
    return new ResultSummary("Summarizing..." + List.range(0, util.Random.nextInt(30) + 2).map(_ => genKey()).mkString(" "), Seq(new TextRegion(0, 11), new TextRegion(12, 24)))
  }

  def generateRandomResult(ptype: ProteusType): SearchResult = {
    val accessID = new AccessIdentifier(genKey(), Some(getResourceKey))
    val result = new SearchResult(accessID, Some(ptype), Some("Title: " + genKey()),
      Some(generateRandomSummary), Some(imgURLs(util.Random.nextInt(imgURLs.length))),
      Some(thumbURLs(util.Random.nextInt(thumbURLs.length))),
      Some(extLinks(util.Random.nextInt(extLinks.length))))
    return result
  }

  def genRandomResults(ptype: ProteusType, N: Int): List[SearchResult] = {
    List.range(0, N).map(_ => generateRandomResult(ptype))
  }

  def genRandomDates: LongValueHistogram = {
    new LongValueHistogram(List.range(0, util.Random.nextInt(50)).map(_ => new WeightedDate(util.Random.nextLong(), Some(util.Random.nextDouble))))
  }

  def genRandomTermHist: TermHistogram = {
    new TermHistogram(List.range(0, util.Random.nextInt(50)).map(_ => new WeightedTerm(genKey(), Some(util.Random.nextDouble))))
  }

  def genRandCoordinates: Coordinates = {
    val left = util.Random.nextInt(500)
    val up = util.Random.nextInt(500)
    return new Coordinates(left, up, left + 10, up + 10)
  }

  /** Core Functionality Methods ( MUST BE PROVIDED ) **/

  def runSearch(s: SearchRequest) = {
    Future { new SearchResponse(genRandomResults(ProteusType.Organization, 10), Some("this is an error message")) }
  }

  def runContainerTransform(accessID: AccessIdentifier,
    to_type: ProteusType,
    from_type: ProteusType,
    params: SearchParameters) = {
    Future { new SearchResponse(genRandomResults(ProteusType.Organization, 10), Some("this is an error message")) }
  }

  def runContentsTransform(accessID: AccessIdentifier,
    to_type: ProteusType,
    from_type: ProteusType,
    params: SearchParameters) = {
    Future { new SearchResponse(genRandomResults(ProteusType.Organization, 10), Some("this is an error message")) }
  }

  def runOverlapsTransform(accessID: AccessIdentifier,
    id_type: ProteusType,
    params: SearchParameters) = {
    Future { new SearchResponse(genRandomResults(id_type, 10)) }
  }

  def runOccurAsObjTransform(id: AccessIdentifier,
    from_type: ProteusType,
    term: String,
    params: SearchParameters) = {
    Future { new SearchResponse(genRandomResults(ProteusType.Page, 10), Some("this is an error message")) }
  }

  def runOccurAsSubjTransform(id: AccessIdentifier,
    from_type: ProteusType,
    term: String,
    params: SearchParameters) = {
    Future { new SearchResponse(genRandomResults(ProteusType.Page, 10), Some("this is an error message")) }
  }

  def runOccurHasObjTransform(id: AccessIdentifier,
    from_type: ProteusType,
    term: String,
    params: SearchParameters) = {
    Future { new SearchResponse(genRandomResults(ProteusType.Page, 10)) }
  }

  def runOccurHasSubjTransform(id: AccessIdentifier,
    from_type: ProteusType,
    term: String,
    params: SearchParameters) = {
    Future { new SearchResponse(genRandomResults(ProteusType.Page, 10), Some("this is an error message")) }
  }

  def runNearbyLocationsTransform(accessID: AccessIdentifier,
    radius: Int,
    params: SearchParameters) = {
    Future { new SearchResponse(genRandomResults(ProteusType.Location, 10)) }
  }

  def runDynamicTransform(accessID: AccessIdentifier,
    dtID: DynamicTransformID,
    params: SearchParameters) = {
    Future { new SearchResponse(genRandomResults(ProteusType.Organization, 10)) }
  }

  def lookupCollection(accessID: AccessIdentifier) = {
    if (accessID.resourceId != getResourceKey)
      Future {
        Collection(new AccessIdentifier(accessID.identifier,
          Some(getResourceKey),
          Some("error: lookup with mismatched resourceID")))
      }
    else
      Future { Collection(accessID, Some("Collection: " + genKey()), Some(generateRandomSummary)) }
  }

  def lookupPage(accessID: AccessIdentifier) = {
    if (accessID.resourceId != getResourceKey)
      Future {
        Page(new AccessIdentifier(accessID.identifier,
          Some(getResourceKey),
          Some("error: lookup with mismatched resourceID")),
          creators = Some(List("Will", "Logan")))
      }
    else {

      Future {
        Page(accessID,
          Some("This is a Page title."),
          Some(this.generateRandomSummary),
          Some(imgURLs(util.Random.nextInt(imgURLs.length))),
          Some(thumbURLs(util.Random.nextInt(thumbURLs.length))),
          Some(extLinks(util.Random.nextInt(extLinks.length))),
          Some(genRandomDates),
          Some(genRandomTermHist),
          Some("en"),
          Some("Pretend this is a full text..."),
          Some(List("Will Dabney", "Logan Giorda")),
          Some(42))
      }
    }

  }

  def lookupPicture(accessID: AccessIdentifier): Future[Picture] = {
    if (accessID.resourceId != getResourceKey)
      Future {
        new Picture(new AccessIdentifier(accessID.identifier,
          Some(getResourceKey),
          Some("error: lookup with mismatched resourceID")),
          creators = Some(List("Will", "Logan")))
      }
    else
      Future { new Picture(accessID, Some("Picture: " + genKey()), Some(generateRandomSummary)) }
  }
  def lookupVideo(accessID: AccessIdentifier): Future[Video] = {
    if (accessID.resourceId != getResourceKey)
      return Future { new Video(new AccessIdentifier(accessID.identifier, Some(getResourceKey), Some("error: lookup with mismatched resourceID"))) }
    else
      return Future { new Video(accessID, Some("Video: " + genKey()), Some(generateRandomSummary)) }
  }

  def lookupAudio(accessID: AccessIdentifier): Future[Audio] = {
    if (accessID.resourceId != getResourceKey)
      return Future { new Audio(new AccessIdentifier(accessID.identifier, Some(getResourceKey), Some("error: lookup with mismatched resourceID"))) }
    else
      return Future { new Audio(accessID, Some("Audio: " + genKey()), Some(generateRandomSummary)) }
  }

  def lookupPerson(accessID: AccessIdentifier): Future[Person] = {
    if (accessID.resourceId != getResourceKey)
      return Future { new Person(new AccessIdentifier(accessID.identifier, Some(getResourceKey), Some("error: lookup with mismatched resourceID"))) }
    else
      return Future { new Person(accessID, Some("Person: " + genKey()), Some(generateRandomSummary)) }
  }
  def lookupLocation(accessID: AccessIdentifier): Future[Location] = {
    if (accessID.resourceId != getResourceKey)
      return Future { new Location(new AccessIdentifier(accessID.identifier, Some(getResourceKey), Some("error: lookup with mismatched resourceID"))) }
    else
      return Future { new Location(accessID, Some("Location: " + genKey()), Some(generateRandomSummary)) }
  }
  def lookupOrganization(accessID: AccessIdentifier): Future[Organization] = {
    if (accessID.resourceId != getResourceKey)
      return Future { new Organization(new AccessIdentifier(accessID.identifier, Some(getResourceKey), Some("error: lookup with mismatched resourceID"))) }
    else
      return Future { new Organization(accessID, Some("Organization: " + genKey()), Some(generateRandomSummary)) }
  }

  def supportsDynTransform(dtID: DynamicTransformID): Future[Boolean] = Future { true }

}

class Library(config: ProteusServiceConfig) extends ProteusNodesService.ThriftServer
  with RandomEndPoint {
  val serverName = "Proteus-Library"
  val thriftPort = config.thriftPort
  val resourceID = connectToManager
  println("Library starting... " + thriftPort)
  def getResourceKey: String = resourceID

  def connectToManager: String = {
    val managerDetails = config.manager.split(":")
    val managerHost = managerDetails(0)
    val managerPort = managerDetails(1).toInt
    println("Connecting to: " + managerHost + " " + managerPort)
    val transport = ClientBuilder()
      .name("Proteus")
      .hosts(Seq(new InetSocketAddress(managerHost, managerPort)))
      .codec(ThriftClientFramedCodec())
      .hostConnectionLimit(1)
      .timeout(500.milliseconds)
      .build()

    val client = new ProteusNodesService.FinagledClient(transport, new TBinaryProtocol.Factory)
    val details = new ConnectLibrary(config.thriftHostname, thriftPort,
      supportedTypes = List(ProteusType.Collection, ProteusType.Page, ProteusType.Picture, ProteusType.Video,
        ProteusType.Audio, ProteusType.Person, ProteusType.Location, ProteusType.Organization))
    return (client.connectLibrary(details) map { f => println("Connected as: " + f.resourceId); f.resourceId.getOrElse("") }).get
  }

  def connectLibrary(details: ConnectLibrary): Future[LibraryConnected] = {
    Future { new LibraryConnected(details.requestedKey, Some("This library does not accept remote connections. It acts only as an end point.")) }
  }
}

class Librarian(config: ProteusServiceConfig) extends ProteusNodesService.ThriftServer
  with LibrarianConnectionManager
  with LibrarianLookupManager
  with LibrarianQueryHandler {

  val serverName = "Proteus"
  val thriftPort = config.thriftPort
  println("Librarian starting... " + thriftPort)

}


  


