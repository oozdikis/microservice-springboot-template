package com.service.test.testcases;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.springframework.boot.SpringApplication;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.service.MainApplicationController;
import com.service.controller.ItemAttachmentService;
import com.service.controller.RequestParameterNames;
import com.service.test.MainTestSuite;

/**
 * Testcases to test the creation of an attachment for an item
 * 
 * @author oozdikis
 */
public class TestAttachmentCreate {

	private final String URL = MainTestSuite.SERVICE_URL + ItemAttachmentService.CREATE_ATTACHMENT_ENDPOINT;
	private final String testItemId = "000000000000000000000000";
	private final String testFilename = "testdata/TestPDF.pdf";
	private HttpClient httpClient = HttpClients.createDefault();

	@Test
	public void testCreateAttachment() {
		if (MainTestSuite.server == null) {
			MainTestSuite.server = SpringApplication.run(MainApplicationController.class);
		}
		
		HttpPost httpPostRequest = null;
		try {
			ClassLoader classLoader = TestAttachmentCreate.class.getClassLoader();
			File fileToUpload = new File(classLoader.getResource(testFilename).getFile());
			HttpEntity data = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
					.addTextBody(RequestParameterNames.COLLECTION, MainTestSuite.COLLECTION, ContentType.create("text/plain", Consts.UTF_8))
					.addTextBody(RequestParameterNames.ID, testItemId, ContentType.create("text/plain", Consts.UTF_8))
					.addBinaryBody(RequestParameterNames.ATTACHMENT, fileToUpload, ContentType.DEFAULT_BINARY, fileToUpload.getName())
					.build();

			httpPostRequest = new HttpPost(URL);
			httpPostRequest.setEntity(data);
			HttpResponse response = httpClient.execute(httpPostRequest);

			if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				fail("Unexpected Http response: " + response.getStatusLine().getStatusCode());
			} else {
				HttpEntity entity = response.getEntity();
				String createdFileId = EntityUtils.toString(entity, "UTF-8");
				assertNotNull(createdFileId);
				
				MongoDatabase db = MainTestSuite.MONGO_CLIENT.getDatabase(MainTestSuite.DATABASE_NAME);
				GridFSBucket gridFSBucket = GridFSBuckets.create(db, MainTestSuite.COLLECTION + ItemAttachmentService.SUFFIX_FOR_ATTACHMENTS_COLLECTION);
				Document queryDoc = new Document();
				queryDoc.append("_id", new ObjectId(createdFileId));
				GridFSFindIterable filesOnDB = gridFSBucket.find(queryDoc);
				GridFSFile fileOnDB = filesOnDB.first();
				assertNotNull(fileOnDB);

				Document fileMetadata = fileOnDB.getMetadata();
				assertEquals(testItemId, fileMetadata.get("itemId").toString());

				ByteArrayOutputStream outputStreamForFileOnDB = new ByteArrayOutputStream();
				gridFSBucket.downloadToStream(fileOnDB.getId(), outputStreamForFileOnDB);
				byte[] byteArrayForFileOnDB = outputStreamForFileOnDB.toByteArray();
	            byte[] byteArrayForFileToUpload = IOUtils.toByteArray(new FileInputStream(fileToUpload));
	            assertEquals(byteArrayForFileToUpload.length, byteArrayForFileOnDB.length);
	            assertArrayEquals(byteArrayForFileToUpload, byteArrayForFileOnDB);
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

}
