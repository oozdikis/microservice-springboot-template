package com.service.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.mongodb.MongoClient;
import com.service.MainApplicationController;
import com.service.test.testcases.TestAttachmentCreate;
import com.service.test.testcases.TestAttachmentGet;
import com.service.test.testcases.TestItemCreate;
import com.service.test.testcases.TestItemGet;
import com.service.test.testcases.TestItemsQuery;

/**
 * Main test class that starts the service and executes all test cases.
 * 
 * @author oozdikis
 */

@RunWith(Suite.class)
@SuiteClasses({ TestItemCreate.class, TestItemsQuery.class, TestItemGet.class, TestAttachmentCreate.class, TestAttachmentGet.class })
public class MainTestSuite {
	public static MongoClient MONGO_CLIENT = new MongoClient();
	public static final String SERVICE_URL = "http://localhost:8080";
	public static final String DATABASE_NAME = "testdb";
	public static final String COLLECTION = "testcollection";

	public static ConfigurableApplicationContext server = null;

	@BeforeClass
	public static void startServer() {
		server = SpringApplication.run(MainApplicationController.class);
		try {
			MONGO_CLIENT.dropDatabase(MainTestSuite.DATABASE_NAME);
		} catch(Exception ex) {
		}
	}

	@AfterClass
	public static void stopServer() {
		server.stop();
	}
}
