package com.service.util;

import org.springframework.http.HttpStatus;

import com.service.util.ErrorResponseModel;

/**
 * Utility class to manage error response
 * 
 * @author oozdikis
 */
public class ErrorResponseUtil {

	/**
	 * Method to create a JSON String to be returned to the client about an
	 * exception in the API
	 * 
	 * @param ex Exception to deliver to the client
	 * @return JSON String to be returned to the client about the exception
	 */
	public static String createErrorResponseJson(Exception ex) {
		ErrorResponseModel errorMessage = new ErrorResponseModel();
		errorMessage.setReasonCode(HttpStatus.BAD_REQUEST.value());
		errorMessage.setReason(ex.getLocalizedMessage());
		String errorJson = GsonUtil.getGson().toJson(errorMessage);
		return errorJson;
	}
}
