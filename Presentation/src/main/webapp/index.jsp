<%@page
	import="com.redhat.refarch.microservices.presentation.RestClient"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Products</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<head>
<body>
	<c:choose>
		<c:when test="${param.register}">
			<%@include file="register.jsp"%>
		</c:when>
		<c:when test="${param.cart}">
			<%@include file="cart.jsp"%>
		</c:when>
		<c:when test="${param.checkout}">
			<%@include file="checkout.jsp"%>
		</c:when>
		<c:when test="${param.history}">
			<%@include file="history.jsp"%>
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${param.purchase}">
					<%
						RestClient.purchase( request );
					%>
				</c:when>
				<c:when test="${param.registration}">
					<%
						RestClient.register( request );
					%>
				</c:when>
				<c:when test="${param.login}">
					<%
						RestClient.login( request );
					%>
				</c:when>
				<c:when test="${param.logout}">
					<%
						RestClient.logout( request );
					%>
				</c:when>
				<c:when test="${param.completeOrder}">
					<%
						RestClient.completeOrder( request );
					%>
				</c:when>
			</c:choose>
			<%
				RestClient.setProductsAttribute( request );
			%>

			<%@include file="header.jsp"%>

			<%@include file="products.jsp"%>
		</c:otherwise>
	</c:choose>
</body>
</html>