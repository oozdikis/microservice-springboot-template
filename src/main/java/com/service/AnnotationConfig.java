package com.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClient;

/**
 * Spring Boot configuration class to access Spring beans and settings in
 * application.properties file
 * 
 * @author oozdikis
 */
@Configuration
public class AnnotationConfig {
	private static final Logger logger = LoggerFactory.getLogger(AnnotationConfig.class);
	public static final String MONGODB_BEAN_NAME = "mongodb";

	@Value("${database.host}")
	private String databaseHost;

	@Value("${database.port}")
	private Integer databasePort;

	@Bean(name = MONGODB_BEAN_NAME)
	public MongoClient getMongoClient() {
		logger.info("Creating MongoDB connection to host:port = " + databaseHost + ":" + databasePort);
		return new MongoClient(databaseHost, databasePort);
	}

}
