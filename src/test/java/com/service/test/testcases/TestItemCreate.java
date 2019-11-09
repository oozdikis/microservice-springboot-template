package com.service.test.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.springframework.boot.SpringApplication;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.service.MainApplicationController;
import com.service.controller.ItemService;
import com.service.controller.RequestParameterNames;
import com.service.test.MainTestSuite;
import com.service.test.model.ExampleItemModel;
import com.service.util.GsonUtil;

/**
 * Testcases to test the creation of an item
 * 
 * @author oozdikis
 */
public class TestItemCreate {
	
	private String URL = MainTestSuite.SERVICE_URL + ItemService.CREATE_ITEM_ENDPOINT;
	private HttpClient httpClient = HttpClients.createDefault();
	
	@Test
	public void testItemCreate() {
		if (MainTestSuite.server == null) {
			MainTestSuite.server = SpringApplication.run(MainApplicationController.class);
		}

		HttpPost httpPostRequest = null;
		try {
			ExampleItemModel itemToCreate = new ExampleItemModel("Özer", "software engineer");
			String itemToCreateJson = GsonUtil.getGson().toJson(itemToCreate);
			
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair(RequestParameterNames.COLLECTION, MainTestSuite.COLLECTION));
		    postParameters.add(new BasicNameValuePair(RequestParameterNames.PAYLOAD, itemToCreateJson));
		    
			httpPostRequest = new HttpPost(URL);
			httpPostRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
			HttpResponse response = httpClient.execute(httpPostRequest);
			
			if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				fail("Unexpected Http response: " + response.getStatusLine().getStatusCode());
			} else {
				HttpEntity entity = response.getEntity();
				String createdItemId = EntityUtils.toString(entity, "UTF-8");
				assertNotNull(createdItemId);
				
				// Search for the created item data in Database. Make sure attributes are stored in DB with correct encoding.
				MongoDatabase db = MainTestSuite.MONGO_CLIENT.getDatabase(MainTestSuite.DATABASE_NAME);
				MongoCollection<Document> collection = db.getCollection(MainTestSuite.COLLECTION);
				Document queryDoc = new Document();
				queryDoc.append("_id", new ObjectId(createdItemId));
				FindIterable<Document> documentsInDB = collection.find();
				assertNotNull(documentsInDB);
				Document documentInDB = documentsInDB.first();
				assertEquals("Özer", documentInDB.get("name"));
				assertEquals("software engineer", documentInDB.get("description"));
			}
			try {
				MainTestSuite.MONGO_CLIENT.dropDatabase(MainTestSuite.DATABASE_NAME);
			} catch(Exception ex) {
			}
		} catch(Exception ex) {
			fail("Exception in test: " + ex.getLocalizedMessage());
		} finally {
			if (httpPostRequest != null) {
				httpPostRequest.releaseConnection();
			}
		}
	}
}
