package com.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The Main class that reads API configurations from the given API settings file
 * and starts listening at the port number according to the settings.
 * 
 * @author oozdikis
 *
 */
@SpringBootApplication
public class MainApplicationController {

	public static void main(String[] args) {
        SpringApplication.run(MainApplicationController.class, args);
    }
}
