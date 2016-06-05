package com.redhat.refarch.microservices.sales.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
public class OrderItem
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long sku;
	private Integer quantity;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "ORDER_ID", referencedColumnName = "ID")
	private Order order;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public Long getSku()
	{
		return sku;
	}

	public void setSku(Long sku)
	{
		this.sku = sku;
	}

	public Integer getQuantity()
	{
		return quantity;
	}

	public void setQuantity(Integer quantity)
	{
		this.quantity = quantity;
	}

	//Avoid getter so it is not included in automatic serialization
	public Order retrieveOrder()
	{
		return order;
	}

	public void setOrder(Order order)
	{
		this.order = order;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		OrderItem other = (OrderItem)obj;
		if( id == null )
		{
			if( other.id != null )
				return false;
		}
		else if( !id.equals( other.id ) )
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "OrderItem [id=" + id + ", sku=" + sku + ", quantity=" + quantity + "]";
	}
}