namespace java edu.ciir.proteus.thrift
namespace rb Searchbird



enum ProteusType {
	COLLECTION,
	PAGE,
	PICTURE,
	VIDEO,
	AUDIO,
	PERSON,
	LOCATION,
	ORGANIZATION,
}

/**  commented out for now....a bug?

const map<ProteusType, list<ProteusType>> contents_map = {ProteusType.COLLECTION : [ProteusType.PAGE], 
														  ProteusType.PAGE : [ProteusType.PICTURE, ProteusType.VIDEO, ProteusType.AUDIO], 
														  ProteusType.PICTURE : [ProteusType.PERSON, ProteusType.LOCATION, ProteusType.ORGANIZATION],
														  ProteusType.VIDEO : [ProteusType.PERSON, ProteusType.LOCATION, ProteusType.ORGANIZATION],
														  ProteusType.AUDIO : [ProteusType.PERSON, ProteusType.LOCATION, ProteusType.ORGANIZATION],
														  ProteusType.PERSON : [], ProteusType.LOCATION : [], ProteusType.ORGANIZATION : []}
														  
const map<ProteusType, list<ProteusType>> container_map = {ProteusType.COLLECTION : [], 
														  ProteusType.PAGE : [ProteusType.COLLECTION], 
														  ProteusType.PICTURE : [ProteusType.PAGE],
														  ProteusType.VIDEO : [ProteusType.PAGE],
														  ProteusType.AUDIO : [ProteusType.PAGE],
														  ProteusType.PERSON : [ProteusType.PICTURE, ProteusType.VIDEO, ProteusType.AUDIO], 
														  ProteusType.LOCATION : [ProteusType.PICTURE, ProteusType.VIDEO, ProteusType.AUDIO], 
														  ProteusType.ORGANIZATION : [ProteusType.PICTURE, ProteusType.VIDEO, ProteusType.AUDIO]
														  }

const map<string, ProteusType> string_map = {"collection" : ProteusType.COLLECTION, 
												"page" :  ProteusType.PAGE, 
												"picture" : ProteusType.PICTURE,
												"video" : ProteusType.VIDEO,
												"audio" : ProteusType.AUDIO,
												"person" : ProteusType.PERSON, 
												"location" : ProteusType.LOCATION, 
												"organization" : ProteusType.ORGANIZATION}		
	**/											   

// **** Query Type structs *********************

// Parameters used in searching and transformations
struct SearchParameters {
	1: optional i16 num_requested = 100; 	// Maximum number of results to return
	2: optional i16 start_at = 0; 			// Return results starting from this index in rank order
	3: optional string language = "en"; 	// Restrict results to this language
}

// Defines a search query to run
struct SearchRequest {
	1: required string query; 				// Query Text
	2: list<ProteusType> types;	 			// Proteus Type to search for
	3: optional SearchParameters params; 	// Parameters of search request
}

// **** Response Type structs ******************

// Defines a region in text
struct TextRegion {
	1: required i32 start; 	// Starting character index
	2: required i32 stop; 	// Ending index (non-inclusive)
}

// A Chunk of text with optional highlighting of regions
struct ResultSummary {
	1: required string text;
	2: list<TextRegion> highlights;
}

// Information needed to access a specific resource item
struct AccessIdentifier {
	1: required string identifier; 			// Ident. for the accessing a result, used for lookup of object
	2: optional string resource_id; 		// Indicates which library/resource this was pulled from
	3: optional string error; 				// Provide error structs for any object passing, lookup not found errors report here
}

// Defines a single result item from a search query
struct SearchResult {
	1: required AccessIdentifier id;
	2: optional ProteusType proteus_type = ProteusType.COLLECTION; 	// Proteus type that this result belongs to
	3: optional string title; 							// Title of the result
	4: optional ResultSummary summary;					// Summary of the contents of this result. May come from surrounding text.
	5: optional string img_url;							// URL of image depiction of this result's data
	6: optional string thumb_url; 						// URL of thumbnail image depiction of this result's data
	7: optional string external_url; 					// URL for visiting the original data source for this item
}

// The response struct to a search query
struct SearchResponse {
	1: list<SearchResult> results;	 	// List of rank ordered search results
	2: optional string error; 			// Error string, empty if no errors
}

struct WeightedTerm {
	1: required string term;
	2: optional double weight = 1.0;
}

// A list of string (term) : weight (frequency) pairs. Used for language models, and other things.
struct TermHistogram {
	1: list<WeightedTerm> terms;
}

struct WeightedDate {
	1: required i64 date = 1;
	2: optional double weight = 1.0;
}

// A list of long values (dates) : weight (frequency) pairs. Used for date mentions, and other things.
struct LongValueHistogram {
	1: list<WeightedDate> dates;
}

// **** Proteus Typed Data Structures **************

// Collection: Book, Newspaper, Website, etc. (highest level container of data)
struct Collection {
	1: required AccessIdentifier id;
	2: optional string title;
	3: optional ResultSummary summary;
	4: optional string img_url;
	5: optional string thumb_url;
	6: optional string external_url;
	7: optional LongValueHistogram date_freq; 	// Dates mentioned in this collection
	8: optional TermHistogram language_model; 	// Language model of this collection (term histogram)
	9: optional string language = "en"; 		// Primary language of this collection

	// Collection specific fields
	10: optional i64 publication_date; 	// Date this collection was published
	11: optional string publisher; 			// Publisher of the collection
	12: optional string edition; 			// Edition this collection represents
	13: optional i32 num_pages;	 		// Number of pages contained by this collection
}

// Page: Page in a book, page of a newspaper, web page on a site, etc.
struct Page {
	1: required AccessIdentifier id;
	2: optional string title;
	3: optional ResultSummary summary;
	4: optional string img_url;
	5: optional string thumb_url;
	6: optional string external_url;

	7: optional LongValueHistogram date_freq; // Dates mentioned
	8: optional TermHistogram language_model;
	9: optional string language = "en";

	// Page specific fields
	10: optional string full_text; 	// The full text of the containing page
	11:	list<string> creators; 	// The creators of this resource
	12: optional i32 page_number; // Where in the collection this page is located
}

// Coordinates of something on a page
struct Coordinates {
	1: required i32 left;
	2: required i32 right;
	3: required i32 up;
	4: required i32 down;
}

struct Picture {
	1: required AccessIdentifier id;
	2: optional string title;
	3: optional ResultSummary summary;
	4: optional string img_url;
	5: optional string thumb_url;
	6: optional string external_url;

	7: optional LongValueHistogram date_freq; 	// Dates mentioned
	8: optional TermHistogram language_model;
	9: optional string language = "en";

	// Picture specific fields
	10: optional string caption;
	11: optional Coordinates coordinates;
	12: list<string> creators; 					// The creators of this resource
}

struct Video {
	1: required AccessIdentifier id;
	2: optional string title;
	3: optional ResultSummary summary;
	4: optional string img_url;
	5: optional string thumb_url;
	6: optional string external_url;

	7: optional LongValueHistogram date_freq; 	// Dates mentioned
	8: optional TermHistogram language_model;
	9: optional string language = "en";

	// Video specific fields
	10: optional string caption;
	11: optional Coordinates coordinates;
	12: list<string> creators; 					// The creators of this resource
	13: i32 length;								// Length of the clip
}

struct Audio {
	1: required AccessIdentifier id;
	2: optional string title;
	3: optional ResultSummary summary;
	4: optional string img_url;
	5: optional string thumb_url;
	6: optional string external_url;

	7: optional LongValueHistogram date_freq; 	// Dates mentioned
	8: optional TermHistogram language_model;
	9: optional string language = "en";

	// Video specific fields
	10: optional string caption;
	11: optional Coordinates coordinates;
	12: list<string> creators; 					// The creators of this resource
	13: i32 length;								// Length of the clip
}


struct Person {
	1: required AccessIdentifier id;
	2: optional string title;
	3: optional ResultSummary summary;
	4: optional string img_url;
	5: optional string thumb_url;
	6: optional LongValueHistogram date_freq; 	// Dates mentioned
	7: optional TermHistogram language_model;

	
	// Person specific fields
	8: optional string full_name; 			// Full name of this person
	9: list<string> alternate_names; 	// List of alternate names, nick names, and aliases
	10: optional string wiki_link; 			// Wikipedia link for this person
	11: optional i64 birth_date; 		// Date of birth
	12: optional i64 death_date; 		// Date of death
}

struct Location {
	1: required AccessIdentifier id;
	2: optional string title;
	3: optional ResultSummary summary;
	4: optional string img_url;
	5: optional string thumb_url;
	6: optional LongValueHistogram date_freq; 	// Dates mentioned
	7: optional TermHistogram language_model;
	
	// Location specific fields
	8: optional string full_name;
	9: list<string> alternate_names;
	10: optional string wiki_link;
	11: optional double longitude; 		// Longitude of this location
	12: optional double latitude; 			// Latitude of this location
}
struct Organization {
	1: required AccessIdentifier id;
	2: optional string title;
	3: optional ResultSummary summary;
	4: optional string img_url;
	5: optional string thumb_url;
	6: optional LongValueHistogram date_freq; 	// Dates mentioned
	7: optional TermHistogram language_model;
	
	// Organization specific fields
	8: optional string full_name;
	9: list<string> alternate_names;
	10: optional string wiki_link;
}


// **** Remote Actor structs / RPC structs ****
struct DynamicTransformID {
	1: required string name;
	2: optional ProteusType from_type;
}


// **** Connection Related structs ****

// Sent by a library to the librarian telling it how to connect/use this library
// No immediate response is expected, but once the library is added the first struct sent will be LibraryConnected
struct ConnectLibrary {
	1: required string hostname;
	2: optional i32 port;
	3: optional string group_id;
	4: optional string requested_key;
	
	5: list<ProteusType> supported_types;
	6: list<DynamicTransformID> dynamic_transforms;
}

struct LibraryConnected {
	1: optional string resource_id;
	2: optional string error;
}


// old Searchbird stuff *****************************
exception SearchbirdException {
  1: string description
}

service SearchbirdService {

  string get(1: string key) throws(1: SearchbirdException ex)
  void put(1: string key, 2: string value)
  list<string> search(1: string query)
}
// end old Searchbird stuff *****************************


/*
 * Services in thrift can inherit from each other...
 *
 */ 
service ProteusAPI {
	
	list<ProteusType> containerFor(1: ProteusType ptype)
	list<ProteusType> convertTypes(1: list<ProteusType> types)

}

/*
 *  Testing scala map creation: with standard sbt-thrift plugin, not working
 								with scrooge plugin, works as expected
 */
 
 //const map<string,string> contents_map = {"hello": "world", "goodnight": "moon"};
 //const map<string,string> container_map = {"hello": "world", "goodnight": "moon"};

/*
 *	The meat.
 * 	
 */
service ProteusNodesService extends ProteusAPI {

	list<ProteusType> getSupportedTypes()
	list<DynamicTransformID> getDynamicTranforms()
	bool supportsType(1: ProteusType ptype)
	bool supportsDynTranform(1: DynamicTransformID dtID);
	
	// Search the libraries for some query
	SearchResponse runSearch(1: SearchRequest s);
	
	// Get the proteus resources that contain this item and are of a certain type
	SearchResponse runContainerTransform(1: AccessIdentifier id, 2: ProteusType from_type, 3: ProteusType to_type, 4: SearchParameters params);
	
	// Get the contents of this item of a certain type
	SearchResponse runContentsTransform(1: AccessIdentifier id, 2: ProteusType from_type, 3: ProteusType to_type, 4: SearchParameters params);
	
	// Find overlapping resources of the same type (Duplicates, Collections of Works, etc.)
	SearchResponse runOverlapsTransform(1: AccessIdentifier id, 2: ProteusType from_type, 3: SearchParameters params);
	
	// Find Pages where the person, location, or organization (specified by ID) occurs as an object of term
	SearchResponse runOccurAsObjTransform(1: AccessIdentifier id, 2: ProteusType from_type, 3: string term, 4: SearchParameters params);
	
	// Find Pages where the person, location, or organization (specified by ID) occurs as the subject of term
	SearchResponse runOccurAsSubjTransform(1: AccessIdentifier id, 2: ProteusType from_type, 3: string term, 4: SearchParameters params);
	
	// Find Pages where the person, location, or organization (specified by ID) occurs having term as its object
	SearchResponse runOccurHasObjTransform(1: AccessIdentifier id, 2: ProteusType from_type, 3: string term, 4: SearchParameters params);
	
	// Find Pages where the person, location, or organization (specified by ID) occurs having term as its subject
	SearchResponse runOccurHasSubjTransform(1: AccessIdentifier id, 2: ProteusType from_type, 3: string term, 4: SearchParameters params);
	
	// Find Locations near the location given by ID
	SearchResponse runNearbyLocationsTransform(1: AccessIdentifier id, 2: i32 radius_miles, 3: SearchParameters params);
	
	// Apply dynamically loaded transformation (name) to id (of type from_type). Result must be a SearchResponse
	// Note: The analogous situation with object variables can be done using extensions, the librarian/manager can stay the same
	SearchResponse runDynamicTransform(1: AccessIdentifier id, 2: DynamicTransformID transform_id, 4: SearchParameters params);
	
	// **** Resource Lookup RPCs ****
	// These all respond with the correctly typed object instantiations
	Collection lookupCollection(1: AccessIdentifier accessID);
	Page lookupPage(1: AccessIdentifier accessID);
	Picture lookupPicture(1: AccessIdentifier accessID);
	Video lookupVideo(1: AccessIdentifier accessID);
	Audio lookupAudio(1: AccessIdentifier accessID);
	Person lookupPerson(1: AccessIdentifier accessID);
	Location lookupLocation(1: AccessIdentifier accessID);
	Organization lookupOrganization(1: AccessIdentifier accessID);

}











