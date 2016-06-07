package com.redhat.refarch.microservices.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;

import com.redhat.refarch.microservices.billing.model.Result;
import com.redhat.refarch.microservices.billing.model.Transaction;
import com.redhat.refarch.microservices.product.model.Inventory;
import com.redhat.refarch.microservices.product.model.Keyword;
import com.redhat.refarch.microservices.product.model.Product;
import com.redhat.refarch.microservices.sales.model.Customer;
import com.redhat.refarch.microservices.sales.model.Order;
import com.redhat.refarch.microservices.sales.model.Order.Status;
import com.redhat.refarch.microservices.sales.model.OrderItem;

public class RestClient {

	private enum Service {
		Product, Sales, Billing
	};

	private static final String PATCH_METHOD = "PATCH";

	public static void setProductsAttribute(HttpServletRequest request) {
		try {
			List<Product> products;
			String query = request.getParameter("query");
			if (query == null || query.isEmpty()) {
				products = getFeaturedProducts();
			} else {
				products = searchProducts(query);
			}
			request.setAttribute("products", products);
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("errorMessage", "Failed to retrieve products: " + e.getMessage());
		}
	}

	private static List<Product> searchProducts(String query) throws HttpErrorException {
		WebTarget webTarget = getWebTarget(Service.Product, "products");
		for (String keyword : query.split("\\s+")) {
			webTarget = webTarget.queryParam("keyword", keyword);
		}
		logInfo("Executing " + webTarget.getUri());
		Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
		if (isError(response)) {
			throw new HttpErrorException(response);
		} else {
			return response.readEntity(new GenericType<List<Product>>() {
			});
		}
	}

	static List<Product> getFeaturedProducts() throws HttpErrorException {
		WebTarget webTarget = getWebTarget(Service.Product, "products").queryParam("featured", true);
		logInfo("Executing " + webTarget.getUri());
		Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
		if (isError(response)) {
			throw new HttpErrorException(response);
		} else {
			return response.readEntity(new GenericType<List<Product>>() {
			});
		}
	}

	public static void register(HttpServletRequest request) throws HttpErrorException {
		WebTarget webTarget = getWebTarget(Service.Sales, "customers");
		Customer customer = Utils.getRegistrationInfo(request);
		Entity<Customer> entity = Entity.entity(customer, MediaType.APPLICATION_JSON);
		logInfo("Executing " + webTarget.getUri() + " with " + customer);
		Response response = webTarget.request(MediaType.APPLICATION_JSON).post(entity);
		if (isError(response)) {
			request.setAttribute("errorMessage", "Failed to register customer");
		} else {
			customer = response.readEntity(Customer.class);
			logInfo("Got " + customer);
			request.getSession().setAttribute("customer", customer);
			request.getSession().setAttribute("itemCount", 0);
			getPendingOrder(request, customer.getId());
		}
	}

	public static void login(HttpServletRequest request) throws HttpErrorException {
		WebTarget webTarget = getWebTarget(Service.Sales, "authenticate");
		Customer customer = Utils.getLoginInfo(request);
		Entity<Customer> entity = Entity.entity(customer, MediaType.APPLICATION_JSON);
		logInfo("Executing " + webTarget.getUri() + " with " + customer);
		Response response = webTarget.request(MediaType.APPLICATION_JSON).post(entity);
		if (isError(response)) {
			int responseCode = response.getStatus();
			if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
				request.setAttribute("errorMessage", "Incorrect password");
			} else if (responseCode == HttpStatus.SC_NOT_FOUND) {
				request.setAttribute("errorMessage", "Customer not found");
				request.setAttribute("username", customer.getUsername());
			} else {
				request.setAttribute("errorMessage", "Failed to login");
			}
		} else {
			customer = response.readEntity(Customer.class);
			logInfo("Got login response " + customer);
			request.getSession().setAttribute("customer", customer);
			request.getSession().setAttribute("itemCount", 0);
			getPendingOrder(request, customer.getId());
		}
	}

	private static void getPendingOrder(HttpServletRequest request, long customerId) {
		WebTarget webTarget = getWebTarget(Service.Sales, "customers", customerId, "orders").queryParam("status",
				Status.Initial.name());
		logInfo("Executing " + webTarget.getUri());
		Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
		if (isError(response)) {
			logInfo("Failed to get pending order: " + response.readEntity(String.class));
		} else {
			List<Order> orders = response.readEntity(new GenericType<List<Order>>() {
			});
			logInfo("Got orders " + orders);
			if (orders.isEmpty()) {
				request.getSession().removeAttribute("orderId");
				request.getSession().removeAttribute("orderItems");
				request.getSession().setAttribute("itemCount", 0);
				request.removeAttribute("cart");
			} else {
				// Not expecting more than one pending order at a time:
				Order order = orders.get(0);
				request.getSession().setAttribute("orderId", order.getId());
				request.getSession().setAttribute("orderItems", order.getOrderItems());
				int cartSize = 0;
				try {
					updateInventory(request, order.getOrderItems());
					for (OrderItem orderItem : order.getOrderItems()) {
						cartSize += orderItem.getQuantity();
					}
				} catch (HttpErrorException e) {
					logInfo("Failed to update inventory: " + e.getMessage());
					e.printStackTrace();
				}
				request.getSession().setAttribute("itemCount", cartSize);
				if (cartSize == 0) {
					request.removeAttribute("cart");
				}
			}
		}
	}

	private static void updateInventory(HttpServletRequest request, List<OrderItem> orderItems)
			throws HttpErrorException {
		@SuppressWarnings("unchecked")
		Map<Long, Product> inventory = (Map<Long, Product>) request.getSession().getAttribute("inventory");
		if (inventory == null) {
			inventory = new HashMap<>();
		}
		for (OrderItem orderItem : orderItems) {
			Product product = lookupProduct(orderItem.getSku());
			inventory.put(product.getSku(), product);
		}
		request.getSession().setAttribute("inventory", inventory);
	}

	private static Product lookupProduct(Long sku) throws HttpErrorException {
		WebTarget webTarget = getWebTarget(Service.Product, "products", sku);
		logInfo("Executing " + webTarget.getUri());
		Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
		if (isError(response)) {
			throw new HttpErrorException(response);
		} else {
			Product product = response.readEntity(Product.class);
			logInfo("Product looked up as " + product);
			return product;
		}
	}

	public static void logout(HttpServletRequest request) {
		HttpSession session = request.getSession();
		Enumeration<String> attrNames = session.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			session.removeAttribute(attrNames.nextElement());
		}
	}

	public static void purchase(HttpServletRequest request) throws HttpErrorException {
		long sku = Long.valueOf(request.getParameter("sku"));
		int availability = lookupProduct(sku).getAvailability();
		if (availability == 0) {
			request.setAttribute("errorMessage", "The selected item is not available for purchase!");
			return;
		}
		Customer customer = (Customer) request.getSession().getAttribute("customer");
		long customerId = customer.getId();
		Long orderId = (Long) request.getSession().getAttribute("orderId");
		if (orderId == null) {
			orderId = addInitialOrder(customerId);
			addOrderItem(customerId, orderId, sku, 1);
		} else {
			@SuppressWarnings("unchecked")
			List<OrderItem> orderItems = (List<OrderItem>) request.getSession().getAttribute("orderItems");
			Optional<OrderItem> optionalItem = orderItems.stream().filter(x -> x.getSku().equals(sku)).findFirst();
			if (optionalItem.isPresent()) {
				OrderItem orderItem = optionalItem.get();
				long orderItemId = orderItem.getId();
				int quantity = orderItem.getQuantity() + 1;
				updateOrderItem(request, customerId, orderId, orderItemId, sku, quantity);
			} else {
				addOrderItem(customerId, orderId, sku, 1);
			}
		}
		getPendingOrder(request, customerId);
	}

	private static long addInitialOrder(long customerId) throws HttpErrorException {
		WebTarget webTarget = getWebTarget(Service.Sales, "customers", customerId, "orders");
		Order order = new Order();
		order.setStatus(Status.Initial);
		Entity<Order> entity = Entity.entity(order, MediaType.APPLICATION_JSON);
		logInfo("Executing " + webTarget.getUri() + " with " + order);
		Response response = webTarget.request(MediaType.APPLICATION_JSON).post(entity);
		if (isError(response)) {
			HttpErrorException exception = new HttpErrorException(response);
			logInfo("Failed to add initial order: " + exception.getMessage());
			throw exception;
		} else {
			Order initialOrder = response.readEntity(Order.class);
			logInfo("Stored intial order as " + initialOrder);
			return initialOrder.getId();
		}
	}

	private static long addOrderItem(long customerId, long orderId, long sku, int quantity) throws HttpErrorException {
		WebTarget webTarget = getWebTarget(Service.Sales, "customers", customerId, "orders", orderId, "orderItems");
		OrderItem orderItem = new OrderItem();
		orderItem.setSku(sku);
		orderItem.setQuantity(quantity);
		Entity<OrderItem> entity = Entity.entity(orderItem, MediaType.APPLICATION_JSON);
		logInfo("Executing " + webTarget.getUri() + " with " + orderItem);
		Response response = webTarget.request(MediaType.APPLICATION_JSON).post(entity);
		if (isError(response)) {
			HttpErrorException exception = new HttpErrorException(response);
			logInfo("Failed to add order item: " + exception.getMessage());
			throw exception;
		} else {
			OrderItem addedOrderItem = response.readEntity(OrderItem.class);
			logInfo("Added order item: " + addedOrderItem);
			return addedOrderItem.getId();
		}
	}

	private static void updateOrderItem(HttpServletRequest request, long customerId, long orderId, long orderItemId,
			Long sku, int quantity) throws HttpErrorException {
		if (sku == null) {
			sku = lookupOrderItem(customerId, orderId, orderItemId).getSku();
		}
		int availability = lookupProduct(sku).getAvailability();
		if (quantity > availability) {
			quantity = availability;
			request.setAttribute("errorMessage", "Requested quantity exceeds product availability");
		}
		WebTarget webTarget = getWebTarget(Service.Sales, "customers", customerId, "orders", orderId, "orderItems",
				orderItemId);
		OrderItem orderItem = new OrderItem();
		orderItem.setQuantity(quantity);
		Entity<OrderItem> entity = Entity.entity(orderItem, MediaType.APPLICATION_JSON);
		logInfo("Executing " + webTarget.getUri() + " with " + orderItem);
		Response response = webTarget.request(MediaType.APPLICATION_JSON).build(PATCH_METHOD, entity).invoke();
		if (isError(response)) {
			HttpErrorException exception = new HttpErrorException(response);
			logInfo("Failed to update order item: " + exception.getMessage());
			throw exception;
		} else {
			logInfo("Updated order item: " + response.readEntity(OrderItem.class));
		}
	}

	private static OrderItem lookupOrderItem(long customerId, long orderId, long orderItemId)
			throws HttpErrorException {
		WebTarget webTarget = getWebTarget(Service.Sales, "customers", customerId, "orders", orderId, "orderItems",
				orderItemId);
		logInfo("Executing " + webTarget.getUri());
		Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
		if (isError(response)) {
			throw new HttpErrorException(response);
		} else {
			return response.readEntity(OrderItem.class);
		}
	}

	private static void deleteOrderItem(long customerId, long orderId, long orderItemId) throws HttpErrorException {
		WebTarget webTarget = getWebTarget(Service.Sales, "customers", customerId, "orders", orderId, "orderItems",
				orderItemId);
		logInfo("Deleting " + webTarget.getUri());
		Response response = webTarget.request(MediaType.APPLICATION_JSON).delete();
		logInfo("Got response " + response.getStatus());
		if (isError(response)) {
			throw new HttpErrorException(response);
		}
	}

	public static void updateQuantity(HttpServletRequest request) throws HttpErrorException {
		Customer customer = (Customer) request.getSession().getAttribute("customer");
		long customerId = customer.getId();
		Long orderId = (Long) request.getSession().getAttribute("orderId");
		Long orderItemId = Long.valueOf(request.getParameter("orderItemId"));
		int quantity = Integer.valueOf(request.getParameter("quantity"));
		if (quantity == 0) {
			deleteOrderItem(customerId, orderId, orderItemId);
		} else {
			updateOrderItem(request, customerId, orderId, orderItemId, null, quantity);
		}
		getPendingOrder(request, customerId);
	}

	public static void completeOrder(HttpServletRequest request) throws HttpErrorException {
		Result result = processTransaction(request);
		if (Result.Status.SUCCESS.equals(result.getStatus())) {
			@SuppressWarnings("unchecked")
			List<OrderItem> orderItems = (List<OrderItem>) request.getSession().getAttribute("orderItems");
			try {
				reduceInventory(orderItems);
			} catch (Exception e) {
				refundTransaction(result.getTransactionNumber());
				request.setAttribute("errorMessage", "Insufficient inventory to fulfill order");
				return;
			}
			try {
				markOrderPayment(request, result);
				request.setAttribute("successMessage", "Your order has been processed");
			} catch (Exception e) {
				logInfo("Order " + request.getSession().getAttribute("orderId")
						+ " processed but not updated in the database");
				request.setAttribute("errorMessage", "Order processed. Allow some time for update!");
			}
			request.getSession().removeAttribute("orderId");
			request.getSession().removeAttribute("orderItems");
			request.getSession().setAttribute("itemCount", 0);
		} else if (Result.Status.FAILURE.equals(result.getStatus())) {
			request.setAttribute("errorMessage", "Your credit card was declined");
		}
	}

	private static Result processTransaction(HttpServletRequest request) throws HttpErrorException {
		WebTarget webTarget = getWebTarget(Service.Billing, "process");
		Transaction transaction = Utils.getTransaction(request);
		Entity<Transaction> entity = Entity.entity(transaction, MediaType.APPLICATION_JSON);
		logInfo("Executing " + webTarget.getUri() + " with " + transaction);
		Response response = webTarget.request(MediaType.APPLICATION_JSON).post(entity);
		if (isError(response)) {
			HttpErrorException exception = new HttpErrorException(response);
			logInfo("Failed to process transaction: " + exception.getMessage());
			throw exception;
		} else {
			Result result = response.readEntity(Result.class);
			logInfo("Got transaction result: " + result);
			return result;
		}
	}

	private static void refundTransaction(long transactionNumber) throws HttpErrorException {
		WebTarget webTarget = getWebTarget(Service.Billing, "refund", transactionNumber);
		logInfo("Executing " + webTarget.getUri());
		Response response = webTarget.request(MediaType.APPLICATION_JSON).post(Entity.json(null));
		logInfo("Transaction refund response code: " + response.getStatus());
		if (isError(response)) {
			HttpErrorException exception = new HttpErrorException(response);
			logInfo("Failed to process transaction: " + exception.getMessage());
			throw exception;
		}
	}

	private static void reduceInventory(List<OrderItem> orderItems) throws HttpErrorException {
		WebTarget webTarget = getWebTarget(Service.Product, "reduce");
		List<Inventory> inventoryList = new ArrayList<>();
		for (OrderItem orderItem : orderItems) {
			Inventory inventory = new Inventory();
			inventory.setSku(orderItem.getSku());
			inventory.setQuantity(orderItem.getQuantity());
			inventoryList.add(inventory);
		}
		Entity<List<Inventory>> entity = Entity.entity(inventoryList, MediaType.APPLICATION_JSON);
		logInfo("Executing " + webTarget.getUri() + " with " + inventoryList);
		Response response = webTarget.request(MediaType.APPLICATION_JSON).post(entity);
		if (isError(response)) {
			HttpErrorException exception = new HttpErrorException(response);
			logInfo("Failed to reduce inventory: " + exception.getMessage());
			throw exception;
		}
	}

	private static void markOrderPayment(HttpServletRequest request, Result transactionResult)
			throws HttpErrorException {
		Customer customer = (Customer) request.getSession().getAttribute("customer");
		Long orderId = transactionResult.getOrderNumber();
		WebTarget webTarget = getWebTarget(Service.Sales, "customers", customer.getId(), "orders", orderId);
		Order order = new Order();
		order.setStatus(Status.Paid);
		order.setTransactionDate(transactionResult.getTransactionDate());
		order.setTransactionNumber(transactionResult.getTransactionNumber());
		Entity<Order> entity = Entity.entity(order, MediaType.APPLICATION_JSON);
		logInfo("Executing " + webTarget.getUri() + " with " + order);
		Response response = webTarget.request(MediaType.APPLICATION_JSON).build(PATCH_METHOD, entity).invoke();
		if (isError(response)) {
			HttpErrorException exception = new HttpErrorException(response);
			logInfo("Failed to mark order payment: " + exception.getMessage());
			throw exception;
		} else {
			Order updatedOrder = response.readEntity(Order.class);
			logInfo("Order updated with payment: " + updatedOrder);
		}
	}

	private static WebTarget getWebTarget(Service service, Object... path) {
		Client client = ClientBuilder.newClient();
		WebTarget target;
		switch (service) {
		case Product:
			target = client.target("http://product-service:8080").path("/product");
			break;

		case Sales:
			target = client.target("http://sales-service:8080").path("/sales");
			break;

		case Billing:
			target = client.target("http://billing-service:8080").path("/billing");
			break;

		default:
			throw new IllegalStateException("Unknown service");
		}
		for (Object part : path) {
			target = target.path(String.valueOf(part));
		}
		return target;
	}

	public static void getOrderHistory(HttpServletRequest request) throws HttpErrorException {
		Customer customer = (Customer) request.getSession().getAttribute("customer");
		WebTarget webTarget = getWebTarget(Service.Sales, "customers", customer.getId(), "orders");
		logInfo("Executing " + webTarget.getUri());
		Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
		if (isError(response)) {
			throw new HttpErrorException(response);
		} else {
			List<Order> orders = response.readEntity(new GenericType<List<Order>>() {
			});
			for (Order order : orders) {
				updateInventory(request, order.getOrderItems());
			}
			Collections.sort(orders, reverseOrderNumberComparator);
			request.setAttribute("orders", orders);
		}
	}

	static long addProduct(Product product) throws HttpErrorException {
		WebTarget webTarget = getWebTarget( Service.Product, "products" );
		Entity<Product> entity = Entity.entity( product, MediaType.APPLICATION_JSON );
		logInfo( "Executing " + webTarget.getUri() + " with " + product );
		Response response = webTarget.request( MediaType.APPLICATION_JSON ).post( entity );
		if( isError( response ) ) {
			HttpErrorException exception = new HttpErrorException( response );
			logInfo( "Failed to add product: " + exception.getMessage() );
			throw exception;
		} else {
			long sku = response.readEntity( Product.class ).getSku();
			logInfo( "Added product with ID " + sku );
			return sku;
		}
	}

	static void addKeyword(Keyword keyword) throws HttpErrorException {
		WebTarget webTarget = getWebTarget( Service.Product, "keywords" );
		Entity<Keyword> entity = Entity.entity( keyword, MediaType.APPLICATION_JSON );
		logInfo( "Executing " + webTarget.getUri() + " with " + keyword );
		Response response = webTarget.request( MediaType.APPLICATION_JSON ).post( entity );
		if( isError( response ) ) {
			HttpErrorException exception = new HttpErrorException( response );
			logInfo( "Failed to add keyword: " + exception.getMessage() );
			throw exception;
		} else {
			logInfo( "Added keyword " + keyword );
		}
	}

	static void classifyProduct(long sku, List<Keyword> keywords) throws HttpErrorException {
		WebTarget webTarget = getWebTarget( Service.Product, "classify", String.valueOf( sku ) );
		Entity<List<Keyword>> entity = Entity.entity(keywords, MediaType.APPLICATION_JSON);
		logInfo( "Executing " + webTarget.getUri() + " with " + keywords );
		Response response = webTarget.request( MediaType.APPLICATION_JSON ).post( entity );
		if( isError( response ) ) {
			HttpErrorException exception = new HttpErrorException( response );
			logInfo( "Failed to classify products: " + exception.getMessage() );
			throw exception;
		} else {
			logInfo( "Classified " + sku + " with " + keywords );
		}
	}

	private static boolean isError(Response response) {
		if (response.getStatus() >= HttpStatus.SC_BAD_REQUEST) {
			return true;
		} else {
			return false;
		}
	}

	private static void logInfo(String message) {
		Logger.getLogger(RestClient.class.getName()).log(Level.INFO, message);
	}

	private static Comparator<Order> reverseOrderNumberComparator = new Comparator<Order>() {

		@Override
		public int compare(Order order1, Order order2) {
			return (int) (order2.getId() - order1.getId());
		}
	};
}