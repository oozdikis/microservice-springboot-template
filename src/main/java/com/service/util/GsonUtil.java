package com.service.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility class that provides a singleton for JSON construction and parsing.
 * 
 * @author oozdikis
 */
public class GsonUtil {

	private static GsonBuilder gsonBuilder = new GsonBuilder();
	private static Gson gson = null;

	public static Gson getGson() {
		if (gson == null) {
			gson = gsonBuilder.setPrettyPrinting().disableHtmlEscaping().create();
		}
		return gson;
	}
}
