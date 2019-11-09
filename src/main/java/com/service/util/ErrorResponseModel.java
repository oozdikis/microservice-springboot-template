package com.service.util;

/**
 * The class that represents the structure to return exception details in a POST
 * request
 * 
 * @author oozdikis
 *
 */
public class ErrorResponseModel {

	private int reasonCode;
	private String reason;

	public int getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(int reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
