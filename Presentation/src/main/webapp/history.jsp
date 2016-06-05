<%@page
	import="com.redhat.refarch.microservices.presentation.RestClient"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%
	RestClient.getOrderHistory( request );
%>
<c:forEach var="order" items="${orders}">
	<div style="margin-top: 5em; margin-bottom: 1em;">
		<table style="margin: 0px auto;">
			<caption style="margin: 0px auto; font-size: 1.5em; padding: 5px;">Order
				${order.id}, ${order.status}</caption>
			<tr style="font-weight: bold;">
				<td style="border: 1px solid black; padding: 5px;">Product</td>
				<td style="border: 1px solid black; padding: 5px;">Unit Price</td>
				<td style="border: 1px solid black; padding: 5px;">Quantity</td>
				<td style="border: 1px solid black; padding: 5px;">Product Cost</td>
			</tr>
			<c:set var="total" value="${0}" />
			<c:forEach var="product" items="${order.orderItems}">
				<tr style="border: 1px solid black;">
					<td
						style="border: 1px solid black; padding: 5px; text-align: right; max-width: 15em; min-width: 15em;">${product.name}</td>
					<td
						style="border: 1px solid black; padding: 5px; text-align: right;"><fmt:formatNumber
							value="${product.price}" type="currency" groupingUsed="true" /></td>
					<td
						style="border: 1px solid black; padding: 5px; text-align: right;">${product.quantity}</td>
					<td
						style="border: 1px solid black; padding: 5px; text-align: right; min-width: 12em;"><fmt:formatNumber
							value="${product.price * product.quantity}" type="currency"
							groupingUsed="true" /></td>
				</tr>
				<c:set var="total"
					value="${total + product.price * product.quantity}" />
			</c:forEach>
			<tr style="font-weight: bold; margin-top: 1em;">
				<td style="padding: 5px;"><div style="padding-top: 1em;">Grand
						Total:</div></td>
				<td style="padding: 5px;"></td>
				<td style="padding: 5px;"></td>
				<td style="padding: 5px; text-align: right;"><div
						style="padding-top: 1em;">
						<fmt:formatNumber value="${total}" type="currency"
							groupingUsed="true" />
					</div></td>
			</tr>
			<c:if test="${not empty order.transactionNumber}">
				<tr>
					<td style="padding: 5px;">Transaction Number:</td>
					<td style="padding: 5px;"></td>
					<td style="padding: 5px;"></td>
					<td style="padding: 5px; text-align: right;">${order.transactionNumber}</td>
				</tr>
				<tr>
					<td style="padding: 5px;">Transaction Date:</td>
					<td style="padding: 5px;"></td>
					<td style="padding: 5px;"></td>
					<td style="padding: 5px; text-align: right;"><fmt:formatDate
							type="both" value="${order.transactionDate}" /></td>
				</tr>
			</c:if>
		</table>
	</div>
</c:forEach>

<form target="_self" method="post">
	<div style="margin: 0px auto; text-align: center;">
		<button
			style="background-color: LightBlue; font-size: 1em; padding: 5px; margin-left: 20px; margin-right: 20px;">Return</button>
	</div>
</form>