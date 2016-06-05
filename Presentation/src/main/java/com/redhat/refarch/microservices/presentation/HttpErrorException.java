package com.redhat.refarch.microservices.presentation;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

public class HttpErrorException extends Exception {

	private static final long serialVersionUID = 1L;
	private int code;
	private String content;

	public HttpErrorException(Response response) {
		code = response.getStatus();
		try {
			content = response.readEntity(String.class);
		} catch (ProcessingException | IllegalStateException e) {
			content = "Unknown";
		}
	}

	@Override
	public String getMessage() {
		return "HTTP Error " + code + ": " + content;
	}
}