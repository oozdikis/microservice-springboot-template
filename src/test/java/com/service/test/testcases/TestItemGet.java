package com.service.test.testcases;

import static org.junit.Assert.assertEquals;
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
import org.junit.Test;
import org.springframework.boot.SpringApplication;

import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.service.MainApplicationController;
import com.service.controller.ItemService;
import com.service.controller.RequestParameterNames;
import com.service.test.MainTestSuite;
import com.service.test.model.ExampleItemModel;
import com.service.util.GsonUtil;

/**
 * Testcases to test retrieval of a item using item id
 * 
 * @author oozdikis
 */
public class TestItemGet {
	private String URL = MainTestSuite.SERVICE_URL + ItemService.GET_ITEM_ENDPOINT;
	private HttpClient httpClient = HttpClients.createDefault();
	
	@Test
	public void testItemGet() {
		if (MainTestSuite.server == null) {
			MainTestSuite.server = SpringApplication.run(MainApplicationController.class);
		}

		HttpPost httpPostRequest = null;
		try {
			String[] generatedIds = initializeDataInDB();
			
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair(RequestParameterNames.COLLECTION, MainTestSuite.COLLECTION));
		    postParameters.add(new BasicNameValuePair(RequestParameterNames.ID, generatedIds[0]));
		    
		    httpPostRequest = new HttpPost(URL);
		    httpPostRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
			HttpResponse response = httpClient.execute(httpPostRequest);
			
			if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				fail("Unexpected Http response: " + response.getStatusLine().getStatusCode());
			} else {
				HttpEntity entity = response.getEntity();
				String responseString = EntityUtils.toString(entity, "UTF-8");
				JsonObject responseJson = GsonUtil.getGson().fromJson(responseString, JsonObject.class);
				assertEquals(generatedIds[0], responseJson.get("_id").getAsString());
				assertEquals("name1", responseJson.get("name").getAsString());
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
	
	@Test
	public void testItemGetHandleNoResult() {
		if (MainTestSuite.server == null) {
			MainTestSuite.server = SpringApplication.run(MainApplicationController.class);
		}

		HttpPost httpPostRequest = null;
		try {
			String nonExistingItemId = "000000000000000000000000";
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair(RequestParameterNames.COLLECTION, MainTestSuite.COLLECTION));
		    postParameters.add(new BasicNameValuePair(RequestParameterNames.ID, nonExistingItemId));
		    
		    httpPostRequest = new HttpPost(URL);
		    httpPostRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
			HttpResponse response = httpClient.execute(httpPostRequest);
			
			if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				fail("Unexpected Http response: " + response.getStatusLine().getStatusCode());
			} else {
				HttpEntity entity = response.getEntity();
				String responseString = EntityUtils.toString(entity, "UTF-8");
				assertEquals("{}", responseString);
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
	
	private String[] initializeDataInDB() {
		String[] generatedIds = new String[2];
				
		MongoDatabase db = MainTestSuite.MONGO_CLIENT.getDatabase(MainTestSuite.DATABASE_NAME);
		MongoCollection<Document> mongoCollection = db.getCollection(MainTestSuite.COLLECTION);
		ExampleItemModel item1 = new ExampleItemModel("name1", "desc");
		Document dbObject1 = Document.parse(GsonUtil.getGson().toJson(item1));
		mongoCollection.insertOne(dbObject1);
		String id1 = dbObject1.get("_id").toString();
		generatedIds[0] = id1;

		ExampleItemModel item2 = new ExampleItemModel("name2", "desc");
		Document dbObject2 = Document.parse(GsonUtil.getGson().toJson(item2));
		mongoCollection.insertOne(dbObject2);
		String id2 = dbObject2.get("_id").toString();
		generatedIds[1] = id2;
		
		return generatedIds;
	}
	
}
