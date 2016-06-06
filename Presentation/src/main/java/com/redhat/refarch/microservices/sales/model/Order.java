package com.redhat.refarch.microservices.sales.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity(name = "Orders")
@NamedQueries({@NamedQuery(name = "Order.findByCustomer", query = "SELECT o FROM Orders o WHERE o.customer = :customer"), @NamedQuery(name = "Order.findByOrderStatus", query = "SELECT o FROM Orders o WHERE o.customer = :customer AND o.status = :status")})
public class Order
{

	public enum Status
	{
		Initial, InProgress, Canceled, Paid, Shipped, Completed
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Status status;
	private Long transactionNumber;
	private Date transactionDate;

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "CUSTOMER_ID", referencedColumnName = "ID")
	private Customer customer;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "order")
	private List<OrderItem> orderItems;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	//Avoid getter so it is not included in automatic serialization
	public Customer retrieveCustomer()
	{
		return customer;
	}

	public void setCustomer(Customer customer)
	{
		this.customer = customer;
	}

	public List<OrderItem> getOrderItems()
	{
		return orderItems;
	}

	public Long getTransactionNumber()
	{
		return transactionNumber;
	}

	public void setTransactionNumber(Long transactionNumber)
	{
		this.transactionNumber = transactionNumber;
	}

	public Date getTransactionDate()
	{
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate)
	{
		this.transactionDate = transactionDate;
	}

	@Override
	public String toString()
	{
		return "Order [id=" + id + ", status=" + status + ", transactionNumber=" + transactionNumber + ", transactionDate=" + transactionDate
				+ ", customer=" + customer + ", orderItems=" + orderItems + "]";
	}
}