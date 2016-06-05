package com.redhat.refarch.microservices.billing.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Transaction
{

	private Long creditCardNumber;
	private Integer expMonth;
	private Integer expYear;
	private Integer verificationCode;
	private String billingAddress;
	private String customerName;
	private Long orderNumber;
	private Double amount;

	public Long getCreditCardNumber()
	{
		return creditCardNumber;
	}

	public void setCreditCardNumber(Long creditCardNumber)
	{
		this.creditCardNumber = creditCardNumber;
	}

	public Integer getExpMonth()
	{
		return expMonth;
	}

	public void setExpMonth(Integer expMonth)
	{
		this.expMonth = expMonth;
	}

	public Integer getExpYear()
	{
		return expYear;
	}

	public void setExpYear(Integer expYear)
	{
		this.expYear = expYear;
	}

	public Integer getVerificationCode()
	{
		return verificationCode;
	}

	public void setVerificationCode(Integer verificationCode)
	{
		this.verificationCode = verificationCode;
	}

	public String getBillingAddress()
	{
		return billingAddress;
	}

	public void setBillingAddress(String billingAddress)
	{
		this.billingAddress = billingAddress;
	}

	public String getCustomerName()
	{
		return customerName;
	}

	public void setCustomerName(String customerName)
	{
		this.customerName = customerName;
	}

	public Long getOrderNumber()
	{
		return orderNumber;
	}

	public void setOrderNumber(Long orderNumber)
	{
		this.orderNumber = orderNumber;
	}

	public Double getAmount()
	{
		return amount;
	}

	public void setAmount(Double amount)
	{
		this.amount = amount;
	}

	@Override
	public String toString()
	{
		return "Transaction [creditCardNumber=" + creditCardNumber + ", expMonth=" + expMonth + ", expYear=" + expYear + ", verificationCode="
				+ verificationCode + ", billingAddress=" + billingAddress + ", customerName=" + customerName + ", orderNumber=" + orderNumber
				+ ", amount=" + amount + "]";
	}
}