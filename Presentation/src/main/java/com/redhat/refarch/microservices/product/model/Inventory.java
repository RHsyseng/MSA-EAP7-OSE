package com.redhat.refarch.microservices.product.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Inventory
{

	private long sku;
	private int quantity;

	public long getSku()
	{
		return sku;
	}

	public void setSku(long sku)
	{
		this.sku = sku;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	@Override
	public String toString()
	{
		return "Inventory [sku=" + sku + ", quantity=" + quantity + "]";
	}
}