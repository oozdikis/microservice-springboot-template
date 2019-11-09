package com.service.test.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;

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

import com.google.gson.JsonArray;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.service.MainApplicationController;
import com.service.controller.ItemService;
import com.service.controller.RequestParameterNames;
import com.service.test.MainTestSuite;
import com.service.test.model.ExampleItemModel;
import com.service.util.GsonUtil;

/**
 * Testcases to test querying items according to query parameters   
 * 
 * @author oozdikis
 */
public class TestItemsQuery {
	private String URL = MainTestSuite.SERVICE_URL + ItemService.QUERY_ITEMS_ENDPOINT;
	private HttpClient httpClient = HttpClients.createDefault();

	@Test
	public void testQueryItems() {
		if (MainTestSuite.server == null) {
			MainTestSuite.server = SpringApplication.run(MainApplicationController.class);
		}
		
		HttpPost httpPostRequest = null;
		try {
			initializeDataInDB();

			ExampleItemModel itemQueryData = new ExampleItemModel();
			itemQueryData.setDescription("desc");	// Service must return 2 results for this search criteria 
			String itemQueryJson = GsonUtil.getGson().toJson(itemQueryData);

			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair(RequestParameterNames.COLLECTION, MainTestSuite.COLLECTION));
			postParameters.add(new BasicNameValuePair(RequestParameterNames.QUERY, itemQueryJson));

			httpPostRequest = new HttpPost(URL);
			httpPostRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
			HttpResponse response = httpClient.execute(httpPostRequest);

			if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				fail("Unexpected Http response: " + response.getStatusLine().getStatusCode());
			} else {
				HttpEntity entity = response.getEntity();
				String responseString = EntityUtils.toString(entity, "UTF-8");
				JsonArray responseJsonArray = GsonUtil.getGson().fromJson(responseString, JsonArray.class);
				assertEquals(2, responseJsonArray.size());

				ExampleItemModel item = GsonUtil.getGson().fromJson(responseJsonArray.get(0).getAsString(), ExampleItemModel.class);
				assertTrue(Arrays.asList("name1", "name2").contains(item.getName()));
				if (!item.getName().equals("name1")) {	// In case the first element in JSON is not policy1, it must be the second element. 
					item = GsonUtil.getGson().fromJson(responseJsonArray.get(1).getAsString(), ExampleItemModel.class);
				}
				assertEquals("name1", item.getName());
				assertEquals("desc", item.getDescription());
			}
			try {
				MainTestSuite.MONGO_CLIENT.dropDatabase(MainTestSuite.DATABASE_NAME);
			} catch(Exception ex) {
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
	public void testQueryItemsHandleNoResult() {
		if (MainTestSuite.server == null) {
			MainTestSuite.server = SpringApplication.run(MainApplicationController.class);
		}

		HttpPost httpPostRequest = null;
		try {
			ExampleItemModel itemQueryData = new ExampleItemModel();
			itemQueryData.setName("there_is_no_such_name");
			String itemQueryJson = GsonUtil.getGson().toJson(itemQueryData);

			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair(RequestParameterNames.COLLECTION, MainTestSuite.COLLECTION));
			postParameters.add(new BasicNameValuePair(RequestParameterNames.QUERY, itemQueryJson));

			httpPostRequest = new HttpPost(URL);
			httpPostRequest.setEntity(new UrlEncodedFormEntity(postParameters, "UTF-8"));
			HttpResponse response = httpClient.execute(httpPostRequest);

			if (response == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				fail("Unexpected Http response: " + response.getStatusLine().getStatusCode());
			} else {
				HttpEntity entity = response.getEntity();
				String responseString = EntityUtils.toString(entity, "UTF-8");
				JsonArray responseJsonArray = GsonUtil.getGson().fromJson(responseString, JsonArray.class);
				assertEquals(0, responseJsonArray.size());
			}
			try {
				MainTestSuite.MONGO_CLIENT.dropDatabase(MainTestSuite.DATABASE_NAME);
			} catch(Exception ex) {
			}

		} catch (Exception ex) {
			fail("Exception in test: " + ex.getLocalizedMessage());
		} finally {
			if (httpPostRequest != null) {
				httpPostRequest.releaseConnection();
			}
		}
	}

	private void initializeDataInDB() {
		MongoDatabase db = MainTestSuite.MONGO_CLIENT.getDatabase(MainTestSuite.DATABASE_NAME);
		MongoCollection<Document> mongoCollection = db.getCollection(MainTestSuite.COLLECTION);
		ExampleItemModel item1 = new ExampleItemModel("name1", "desc");
		Document dbObject1 = Document.parse(GsonUtil.getGson().toJson(item1));
		mongoCollection.insertOne(dbObject1);

		ExampleItemModel item2 = new ExampleItemModel("name2", "desc");
		Document dbObject2 = Document.parse(GsonUtil.getGson().toJson(item2));
		mongoCollection.insertOne(dbObject2);
	}

}
