package com.redhat.refarch.microservices.presentation;

import javax.servlet.http.HttpServletRequest;

import com.redhat.refarch.microservices.billing.model.Transaction;
import com.redhat.refarch.microservices.sales.model.Customer;

public class Utils {

	public static Customer getRegistrationInfo(HttpServletRequest request) {
		Customer customer = new Customer();
		customer.setName(request.getParameter("name"));
		customer.setAddress(request.getParameter("address"));
		customer.setTelephone(request.getParameter("telephone"));
		customer.setEmail(request.getParameter("email"));
		customer.setUsername(request.getParameter("username"));
		customer.setPassword(request.getParameter("password"));
		return customer;
	}

	public static Customer getLoginInfo(HttpServletRequest request) {
		Customer customer = new Customer();
		customer.setUsername(request.getParameter("username"));
		customer.setPassword(request.getParameter("password"));
		return customer;
	}

	public static Transaction getTransaction(HttpServletRequest request) {
		Customer customer = (Customer) request.getSession().getAttribute("customer");
		Transaction transaction = new Transaction();
		transaction.setAmount(Double.valueOf(request.getParameter("amount")));
		transaction.setCreditCardNumber(Long.valueOf(request.getParameter("creditCardNo")));
		transaction.setExpMonth(Integer.valueOf(request.getParameter("expirationMM")));
		transaction.setExpYear(Integer.valueOf(request.getParameter("expirationYY")));
		transaction.setVerificationCode(Integer.valueOf(request.getParameter("verificationCode")));
		transaction.setBillingAddress(customer.getAddress());
		transaction.setCustomerName(customer.getName());
		transaction.setOrderNumber((Long) request.getSession().getAttribute("orderId"));
		return transaction;
	}
}