package com.service.controller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

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
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.service.AnnotationConfig;
import com.service.util.ErrorResponseUtil;

/**
 * Implementation of {@link RestController} that initializes two endpoints to
 * upload/download attachment files for an item.
 * 
 * @author oozdikis
 */
@RestController
public class ItemAttachmentService {

	/**
	 * Attachments for an item collection will be stored in MongoDB collection with name =
	 * <itemcollection>.attachments
	 */
	public static final String SUFFIX_FOR_ATTACHMENTS_COLLECTION = ".attachments";
	public static final String CREATE_ATTACHMENT_ENDPOINT = "/api/createAttachment";
	public static final String GET_ATTACHMENT_ENDPOINT = "/api/getAttachment";
	private static final Logger logger = LoggerFactory.getLogger(ItemAttachmentService.class);

	@Autowired
	private ApplicationContext applicationContext;

	@Value("${database.name}")
	private String databaseName;

	/**
	 * Method to upload an attachment file for an item
	 * 
	 * @param collection Name of the items collection
	 * @param itemId id of the item as String
	 * @param attachedFile file to upload as Multipartfile sent through POST request
	 * @return id of the created file as String
	 */
	@RequestMapping(value = CREATE_ATTACHMENT_ENDPOINT, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> createAttachment(@RequestParam(RequestParameterNames.COLLECTION) String collection,
			@RequestParam(RequestParameterNames.ID) String itemId,
			@RequestParam(RequestParameterNames.ATTACHMENT) MultipartFile attachedFile) {
		try {
			logger.debug("Request received for createAttachment for item collection: " + collection);
			InputStream initialStream = attachedFile.getInputStream();
			String fileName = attachedFile.getOriginalFilename();

			MongoClient mongoClient = applicationContext.getBean(AnnotationConfig.MONGODB_BEAN_NAME, MongoClient.class);
			MongoDatabase db = mongoClient.getDatabase(databaseName);
			Document dbObject = new Document();
			dbObject.append("itemId", itemId);

			GridFSBucket gridFSBucket = GridFSBuckets.create(db, collection + SUFFIX_FOR_ATTACHMENTS_COLLECTION);
			GridFSUploadOptions options = new GridFSUploadOptions().chunkSizeBytes(1024).metadata(dbObject);
			ObjectId fileId = gridFSBucket.uploadFromStream(fileName, initialStream, options);
			logger.debug("Returning created file id: " + fileId.toString());
			return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(fileId.toString());
		} catch (Exception ex) {
			logger.error("Exception while creating attachment in database. ", ex);
			String errorJson = ErrorResponseUtil.createErrorResponseJson(ex);
			return ResponseEntity.badRequest().body(errorJson);
		}
	}

	/**
	 * Method to download the attachment file for a given file id
	 * 
	 * @param collection Name of the items collection
	 * @param fileId id of the file in database as String
	 * @return byte array for the file. If file does not exist, returns an empty array
	 */
	@RequestMapping(value = GET_ATTACHMENT_ENDPOINT, method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> getAttachment(@RequestParam(RequestParameterNames.COLLECTION) String collection,
			@RequestParam(RequestParameterNames.ID) String fileId) {
		try {
			logger.debug("Request received for getAttachment for item collection: " + collection);
			MongoClient mongoClient = applicationContext.getBean(AnnotationConfig.MONGODB_BEAN_NAME, MongoClient.class);
			MongoDatabase db = mongoClient.getDatabase(databaseName);

			GridFSBucket gridFSBucket = GridFSBuckets.create(db, collection + SUFFIX_FOR_ATTACHMENTS_COLLECTION);
			Document queryDoc = new Document();
			queryDoc.append("_id", new ObjectId(fileId));
			GridFSFindIterable filesOnDB = gridFSBucket.find(queryDoc);
			GridFSFile fileOnDB = filesOnDB.first();
			if (fileOnDB == null) {
				logger.debug("Could not find any file with id: " + fileId + ". Returning JSON response with 0 bytes.");
				return ResponseEntity.ok().contentLength(0)
						.contentType(MediaType.parseMediaType("application/octet-stream")).body(new byte[0]);
			} else {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				gridFSBucket.downloadToStream(fileOnDB.getId(), output);
				byte[] bytes = output.toByteArray();
				logger.debug("Returning JSON response for file with length: " + fileOnDB.getLength());
				return ResponseEntity.ok().contentLength(fileOnDB.getLength())
						.contentType(MediaType.parseMediaType("application/octet-stream")).body(bytes);
			}
		} catch (Exception ex) {
			logger.error("Exception while getting attachment from database. ", ex);
			String errorJson = ErrorResponseUtil.createErrorResponseJson(ex);
			return ResponseEntity.badRequest().body(errorJson);
		}
	}
}