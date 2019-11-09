package com.service.controller;

import java.util.ArrayList;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.service.AnnotationConfig;
import com.service.util.ErrorResponseUtil;
import com.service.util.GsonUtil;

/**
 * Implementation of {@link RestController} that initializes three endpoints to
 * create/query/get items from database.
 * 
 * @author oozdikis
 */
@RestController
public class ItemService {

	public static final String CREATE_ITEM_ENDPOINT = "/api/createItem";
	public static final String QUERY_ITEMS_ENDPOINT = "/api/queryItems";
	public static final String GET_ITEM_ENDPOINT = "/api/getItem";
	private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

	@Autowired
	private ApplicationContext applicationContext;

	@Value("${database.name}")
	private String databaseName;

	/**
	 * Method to create an item in a collection.
	 * 
	 * @param collection Name of the collection
	 * @param payload Item to store in database in JSON format
	 * @return Returns the created item id as String
	 */
	@RequestMapping(value = CREATE_ITEM_ENDPOINT, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> createItem(@RequestParam(RequestParameterNames.COLLECTION) String collection,
			@RequestParam(RequestParameterNames.PAYLOAD) String payload) {
		try {
			logger.debug("Request received for create item for collection: " + collection);
			MongoClient mongoClient = applicationContext.getBean(AnnotationConfig.MONGODB_BEAN_NAME, MongoClient.class);
			MongoDatabase db = mongoClient.getDatabase(databaseName);
			MongoCollection<Document> mongoCollection = db.getCollection(collection);
			Document dbObject = Document.parse(payload);
			mongoCollection.insertOne(dbObject);
			String id = dbObject.get("_id").toString();
			logger.debug("Returning created item id: " + id);
			return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(id);
		} catch (Exception ex) {
			logger.error("Exception while creating item in database. ", ex);
			String errorJson = ErrorResponseUtil.createErrorResponseJson(ex);
			return ResponseEntity.badRequest().body(errorJson);
		}
	}

	/**
	 * Method to query items
	 * 
	 * @param collection  Name of the collection
	 * @param queryParams Query parameters as JSON string. JSON structure should be
	 *                    consistent with the item objects in the collection.
	 * @return Array of items in JSON format
	 */
	@RequestMapping(value = QUERY_ITEMS_ENDPOINT, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> queryItems(@RequestParam(RequestParameterNames.COLLECTION) String collection,
			@RequestParam(RequestParameterNames.QUERY) String queryParams) {
		try {
			logger.debug("Request received for queryItems for collection: " + collection);
			MongoClient mongoClient = applicationContext.getBean(AnnotationConfig.MONGODB_BEAN_NAME, MongoClient.class);
			MongoDatabase db = mongoClient.getDatabase(databaseName);
			MongoCollection<Document> mongoCollection = db.getCollection(collection);
			Document dbObject = Document.parse(queryParams);

			FindIterable<Document> docs = mongoCollection.find(dbObject);
			ArrayList<String> docJsonStrings = new ArrayList<String>();
			for (Document doc : docs) {
				ObjectId value = (ObjectId) doc.remove("_id");
				doc.append("_id", value.toString());
				docJsonStrings.add(doc.toJson());
			}
			logger.debug("Returning " + docJsonStrings.size() + " items as query result");
			return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
					.body(GsonUtil.getGson().toJson(docJsonStrings));
		} catch (Exception ex) {
			logger.error("Exception while querying items from database. ", ex);
			String errorJson = ErrorResponseUtil.createErrorResponseJson(ex);
			return ResponseEntity.badRequest().body(errorJson);
		}
	}

	/**
	 * Method to get item details for a given item id.
	 * 
	 * @param collection Name of the collection
	 * @param id      item id to get as String
	 * @return Item object in JSON format
	 */
	@RequestMapping(value = GET_ITEM_ENDPOINT, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> getItem(@RequestParam(RequestParameterNames.COLLECTION) String collection,
			@RequestParam(RequestParameterNames.ID) String id) {
		try {
			logger.debug("Request received for getItem for collection: " + collection);
			MongoClient mongoClient = applicationContext.getBean(AnnotationConfig.MONGODB_BEAN_NAME, MongoClient.class);
			MongoDatabase db = mongoClient.getDatabase(databaseName);
			MongoCollection<Document> mongoCollection = db.getCollection(collection);

			Document dbObject = new Document();
			dbObject.append("_id", new ObjectId(id));
			Document foundDocument = (Document) mongoCollection.find(dbObject).first();
			if (foundDocument == null) {
				logger.debug("Could not find any item with id: " + id + ". Returning JSON with no entry.");
				return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("{}");
			} else {
				ObjectId uniqueId = (ObjectId) foundDocument.remove("_id");
				foundDocument.append("_id", uniqueId.toString());
				logger.debug("Returning JSON response for item with id: " + id);
				return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(foundDocument.toJson());
			}
		} catch (Exception ex) {
			logger.error("Exception while getting item from database. ", ex);
			String errorJson = ErrorResponseUtil.createErrorResponseJson(ex);
			return ResponseEntity.badRequest().body(errorJson);
		}
	}
}