package com.redhat.refarch.microservices.sales.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.redhat.refarch.microservices.sales.model.Customer;
import com.redhat.refarch.microservices.sales.model.Error;
import com.redhat.refarch.microservices.sales.model.Order;
import com.redhat.refarch.microservices.sales.model.Order.Status;
import com.redhat.refarch.microservices.sales.model.OrderItem;
import com.redhat.refarch.microservices.utils.Utils;

@Stateless
@LocalBean
@Path("/")
public class SalesService
{

	private Logger logger = Logger.getLogger( getClass().getName() );

	@PersistenceContext
	private EntityManager em;

	@POST
	@Path("/customers")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Customer addCustomer(Customer customer)
	{
		try
		{
			em.persist( customer );
			return customer;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@GET
	@Path("/customers")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Customer getCustomer(@QueryParam("username") String username)
	{
		try
		{
			TypedQuery<Customer> query = em.createNamedQuery( "Customer.findByUsername", Customer.class );
			Customer customer = query.setParameter( "username", username ).getSingleResult();
			logInfo( "Customer for " + username + ": " + customer );
			return customer;
		}
		catch( NoResultException e )
		{
			throw new Error( HttpURLConnection.HTTP_NOT_FOUND, "Customer not found" ).asException();
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@GET
	@Path("/customers/{id}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Customer getCustomer(@PathParam("id") Long id)
	{
		try
		{
			logInfo( "Customer Id is " + id );
			Customer customer = em.find( Customer.class, id );
			logInfo( "Customer with ID " + id + " is " + customer );
			if( customer == null )
			{
				throw new Error( HttpURLConnection.HTTP_NOT_FOUND, "Customer not found" ).asException();
			}
			return customer;
		}
		catch( WebApplicationException e )
		{
			throw e;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@PUT
	@Path("/customers/{id}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Customer updateCustomer(@PathParam("id") Long id, Customer customer)
	{
		Customer entity = getCustomer( id );
		try
		{
			//Ignore any attempt to update customer Id:
			customer.setId( id );
			Utils.copy( customer, entity, false );
			em.merge( entity );
			return entity;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@PATCH
	@Path("/customers/{id}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Customer partiallyUpdateCustomer(@PathParam("id") Long id, Customer customer)
	{
		Customer entity = getCustomer( id );
		try
		{
			//Ignore any attempt to update customer Id:
			customer.setId( id );
			Utils.copy( customer, entity, true );
			em.merge( entity );
			return entity;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@DELETE
	@Path("/customers/{id}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public void deleteCustomer(@PathParam("id") Long id)
	{
		Customer entity = getCustomer( id );
		try
		{
			em.remove( entity );
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@POST
	@Path("/customers/{customerId}/orders")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Order addOrder(@PathParam("customerId") Long customerId, Order order)
	{
		Customer customer = getCustomer( customerId );
		order.setCustomer( customer );
		try
		{
			em.persist( order );
			return order;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@GET
	@Path("/customers/{customerId}/orders")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Order> getOrders(@PathParam("customerId") Long customerId, @QueryParam("status") Status status)
	{
		logInfo( "getOrders(" + customerId + ", " + status + ")" );
		Customer customer = getCustomer( customerId );
		try
		{
			TypedQuery<Order> query;
			if( status == null )
			{
				query = em.createNamedQuery( "Order.findByCustomer", Order.class );
			}
			else
			{
				query = em.createNamedQuery( "Order.findByOrderStatus", Order.class );
				query.setParameter( "status", status );
			}
			query.setParameter( "customer", customer );
			List<Order> orders = query.getResultList();
			logInfo( "Orders retrieved as " + orders );
			return orders;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@GET
	@Path("/customers/{customerId}/orders/{orderId}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Order getOrder(@PathParam("customerId") Long customerId, @PathParam("orderId") Long orderId)
	{
		try
		{
			Order order = em.find( Order.class, orderId );
			logInfo( "Order retrieved as " + order );
			if( order != null && customerId.equals( order.retrieveCustomer().getId() ) )
			{
				return order;
			}
			else
			{
				throw new Error( HttpURLConnection.HTTP_NOT_FOUND, "Order not found" ).asException();
			}
		}
		catch( WebApplicationException e )
		{
			throw e;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@PUT
	@Path("/customers/{customerId}/orders/{orderId}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Order updateOrder(@PathParam("customerId") Long customerId, @PathParam("orderId") Long orderId, Order order)
	{
		Order entity = getOrder( customerId, orderId );
		try
		{
			//Ignore any attempt to update order Id:
			order.setId( orderId );
			Utils.copy( order, entity, false );
			em.merge( entity );
			return entity;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@PATCH
	@Path("/customers/{customerId}/orders/{orderId}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Order partiallyUpdateOrder(@PathParam("customerId") Long customerId, @PathParam("orderId") Long orderId, Order order)
	{
		Order entity = getOrder( customerId, orderId );
		try
		{
			//Ignore any attempt to update order Id:
			order.setId( orderId );
			Utils.copy( order, entity, true );
			em.merge( entity );
			return entity;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@DELETE
	@Path("/customers/{customerId}/orders/{orderId}")
	public void deleteOrder(@PathParam("customerId") Long customerId, @PathParam("orderId") Long orderId)
	{
		Order entity = getOrder( customerId, orderId );
		try
		{
			em.remove( entity );
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@POST
	@Path("/customers/{customerId}/orders/{orderId}/orderItems")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public OrderItem addOrderItem(@PathParam("customerId") Long customerId, @PathParam("orderId") Long orderId, OrderItem orderItem)
	{
		Order order = getOrder( customerId, orderId );
		orderItem.setOrder( order );
		try
		{
			em.persist( orderItem );
			return orderItem;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@GET
	@Path("/customers/{customerId}/orders/{orderId}/orderItems")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<OrderItem> getOrderItems(@PathParam("customerId") Long customerId, @PathParam("orderId") Long orderId)
	{
		Order order = getOrder( customerId, orderId );
		if( order == null )
		{
			throw new Error( HttpURLConnection.HTTP_NOT_FOUND, "Order not found" ).asException();
		}
		try
		{
			return order.getOrderItems();
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@GET
	@Path("/customers/{customerId}/orders/{orderId}/orderItems/{orderItemId}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public OrderItem getOrderItem(@PathParam("customerId") Long customerId, @PathParam("orderId") Long orderId,
			@PathParam("orderItemId") Long orderItemId)
	{
		try
		{
			OrderItem orderItem = em.find( OrderItem.class, orderItemId );
			if( orderItem != null && orderId.equals( orderItem.retrieveOrder().getId() )
					&& customerId.equals( orderItem.retrieveOrder().retrieveCustomer().getId() ) )
			{
				return orderItem;
			}
			else
			{
				throw new Error( HttpURLConnection.HTTP_NOT_FOUND, "Order Item not found" ).asException();
			}
		}
		catch( WebApplicationException e )
		{
			throw e;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@PUT
	@Path("/customers/{customerId}/orders/{orderId}/orderItems/{orderItemId}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public OrderItem updateOrderItem(@PathParam("customerId") Long customerId, @PathParam("orderId") Long orderId,
			@PathParam("orderItemId") Long orderItemId, OrderItem orderItem)
	{
		OrderItem entity = getOrderItem( customerId, orderId, orderItemId );
		try
		{
			//Ignore any attempt to update order item Id:
			orderItem.setId( orderItemId );
			Utils.copy( orderItem, entity, false );
			em.merge( entity );
			return entity;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@PATCH
	@Path("/customers/{customerId}/orders/{orderId}/orderItems/{orderItemId}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public OrderItem partiallyUpdateOrderItem(@PathParam("customerId") Long customerId, @PathParam("orderId") Long orderId,
			@PathParam("orderItemId") Long orderItemId, OrderItem orderItem)
	{
		OrderItem entity = getOrderItem( customerId, orderId, orderItemId );
		try
		{
			//Ignore any attempt to update order item Id:
			orderItem.setId( orderItemId );
			Utils.copy( orderItem, entity, true );
			em.merge( entity );
			return entity;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@DELETE
	@Path("/customers/{customerId}/orders/{orderId}/orderItems/{orderItemId}")
	public void deleteOrderItem(@PathParam("customerId") Long customerId, @PathParam("orderId") Long orderId,
			@PathParam("orderItemId") Long orderItemId)
	{
		Order order = getOrder( customerId, orderId );
		OrderItem entity = getOrderItem( customerId, orderId, orderItemId );
		try
		{
			em.remove( entity );
			order.getOrderItems().remove( entity );
			em.merge( order );
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@POST
	@Path("/authenticate")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Customer authenticate(Customer customer)
	{
		logInfo( "Asked to authenticate " + customer );
		Customer response = getCustomer( customer.getUsername() );
		try
		{
			if( response.getPassword().equals( customer.getPassword() ) == false )
			{
				throw new WebApplicationException( HttpURLConnection.HTTP_UNAUTHORIZED );
			}
			return response;
		}
		catch( WebApplicationException e )
		{
			throw e;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	private void logInfo(String message)
	{
		logger.log( Level.INFO, message );
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@HttpMethod("PATCH")
	public @interface PATCH
	{
	}
}