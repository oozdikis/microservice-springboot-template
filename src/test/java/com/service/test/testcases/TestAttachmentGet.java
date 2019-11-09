package com.service.test.testcases;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.springframework.boot.SpringApplication;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.service.MainApplicationController;
import com.service.controller.ItemAttachmentService;
import com.service.controller.RequestParameterNames;
import com.service.test.MainTestSuite;

/**
 * Testcases to test retrieval of an attachment for an item using file id
 * 
 * @author oozdikis
 */
public class TestAttachmentGet {
	
	private final String URL = MainTestSuite.SERVICE_URL + ItemAttachmentService.GET_ATTACHMENT_ENDPOINT;
	private final String testItemId = "000000000000000000000000";
	private final String testFilename = "testdata/TestPDF.pdf";
	private HttpClient httpClient = HttpClients.createDefault();
	
	@Test
	public void testGetAttachment() {
		if (MainTestSuite.server == null) {
			MainTestSuite.server = SpringApplication.run(MainApplicationController.class);
		}

		HttpPost httpPostRequest = null;
		try {
			String generatedFileId = initializeDataInDB();
			
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair(RequestParameterNames.COLLECTION, MainTestSuite.COLLECTION));
		    postParameters.add(new BasicNameValuePair(RequestParameterNames.ID, generatedFileId));
		    
		    httpPostRequest = new HttpPost(URL);
		    httpPostRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
			HttpResponse response = httpClient.execute(httpPostRequest);
			
			if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				fail("Unexpected Http response: " + response.getStatusLine().getStatusCode());
			} else {
				HttpEntity entity = response.getEntity();
				BufferedHttpEntity buf = new BufferedHttpEntity(entity);
				ByteArrayOutputStream outputStreamReceivedFromService = new ByteArrayOutputStream();
				buf.writeTo(outputStreamReceivedFromService);
				byte[] byteArrayReceivedFromService = outputStreamReceivedFromService.toByteArray();
				
				ClassLoader classLoader = TestAttachmentCreate.class.getClassLoader();
				File fileToUpload = new File(classLoader.getResource(testFilename).getFile());
				byte[] byteArrayForFileToUpload = IOUtils.toByteArray(new FileInputStream(fileToUpload));
	            assertEquals(byteArrayForFileToUpload.length, byteArrayReceivedFromService.length);
	            assertArrayEquals(byteArrayForFileToUpload, byteArrayReceivedFromService);
			}
			try {
				MainTestSuite.MONGO_CLIENT.dropDatabase(MainTestSuite.DATABASE_NAME);
			} catch (Exception ex) {
			}
		} catch (Exception ex) {
			fail("Exception in test: " + ex.getLocalizedMessage());
		} finally {
			if (httpPostRequest != null) {
				httpPostRequest.releaseConnection();
			}
		}
	}
	
	@Test
	public void testGetAttachmentHandleNoResult() {
		if (MainTestSuite.server == null) {
			MainTestSuite.server = SpringApplication.run(MainApplicationController.class);
		}

		HttpPost httpPostRequest = null;
		try {
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair(RequestParameterNames.COLLECTION, MainTestSuite.COLLECTION));
		    postParameters.add(new BasicNameValuePair(RequestParameterNames.ID, testItemId));
		    
		    httpPostRequest = new HttpPost(URL);
		    httpPostRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
			HttpResponse response = httpClient.execute(httpPostRequest);
			
			if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				fail("Unexpected Http response: " + response.getStatusLine().getStatusCode());
			} else {
				HttpEntity entity = response.getEntity();
				BufferedHttpEntity buf = new BufferedHttpEntity(entity);
				ByteArrayOutputStream outputStreamReceivedFromService = new ByteArrayOutputStream();
				buf.writeTo(outputStreamReceivedFromService);
				byte[] byteArrayReceivedFromService = outputStreamReceivedFromService.toByteArray();
	            assertEquals(0, byteArrayReceivedFromService.length);
			}
			try {
				MainTestSuite.MONGO_CLIENT.dropDatabase(MainTestSuite.DATABASE_NAME);
			} catch (Exception ex) {
			}
		} catch (Exception ex) {
			fail("Exception in test: " + ex.getLocalizedMessage());
		} finally {
			if (httpPostRequest != null) {
				httpPostRequest.releaseConnection();
			}
		}
	}
	
	private String initializeDataInDB() throws FileNotFoundException {
		ClassLoader classLoader = TestAttachmentCreate.class.getClassLoader();
		File fileToUpload = new File(classLoader.getResource(testFilename).getFile());
		FileInputStream fileInputStream = new FileInputStream(fileToUpload);

		MongoDatabase db = MainTestSuite.MONGO_CLIENT.getDatabase(MainTestSuite.DATABASE_NAME);
		GridFSBucket gridFSBucket = GridFSBuckets.create(db, MainTestSuite.COLLECTION + ItemAttachmentService.SUFFIX_FOR_ATTACHMENTS_COLLECTION);
		Document dbObject = new Document();
		dbObject.append("itemId", testItemId);
		GridFSUploadOptions options = new GridFSUploadOptions().chunkSizeBytes(1024).metadata(dbObject);
		ObjectId fileId = gridFSBucket.uploadFromStream(testFilename, fileInputStream, options);
		return fileId.toString();
	}

}
