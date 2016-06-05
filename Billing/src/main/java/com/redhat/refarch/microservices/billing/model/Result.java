package com.redhat.refarch.microservices.billing.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Result
{

	public enum Status
	{
		SUCCESS, FAILURE
	}

	private Status status;
	private String name;
	private Long orderNumber;
	private Date transactionDate;
	private Long transactionNumber;

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Long getOrderNumber()
	{
		return orderNumber;
	}

	public void setOrderNumber(Long orderNumber)
	{
		this.orderNumber = orderNumber;
	}

	public Date getTransactionDate()
	{
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate)
	{
		this.transactionDate = transactionDate;
	}

	public Long getTransactionNumber()
	{
		return transactionNumber;
	}

	public void setTransactionNumber(Long transactionNumber)
	{
		this.transactionNumber = transactionNumber;
	}

	@Override
	public String toString()
	{
		return "Result [status=" + status + ", name=" + name + ", orderNumber=" + orderNumber + ", transactionDate=" + transactionDate
				+ ", transactionNumber=" + transactionNumber + "]";
	}
}