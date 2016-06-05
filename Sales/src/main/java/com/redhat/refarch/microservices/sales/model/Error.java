package com.redhat.refarch.microservices.sales.model;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Error
{

	private int code;
	private String message;
	private String details;

	@SuppressWarnings("unused")
	private Error()
	{
	}

	public Error(int code, String message, Throwable throwable)
	{
		this.code = code;
		if( message != null )
		{
			this.message = message;
		}
		else if( throwable != null )
		{
			this.message = throwable.getMessage();
		}
		if( throwable != null )
		{
			StringWriter writer = new StringWriter();
			throwable.printStackTrace( new PrintWriter( writer ) );
			this.details = writer.toString();
		}
	}

	public Error(int code, String message)
	{
		this( code, message, null );
	}

	public Error(int code, Throwable throwable)
	{
		this( code, null, throwable );
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getDetails()
	{
		return details;
	}

	public void setDetails(String details)
	{
		this.details = details;
	}

	public WebApplicationException asException()
	{
		ResponseBuilder responseBuilder = Response.status( code );
		responseBuilder = responseBuilder.entity( this );
		return new WebApplicationException( responseBuilder.build() );
	}
}