<%@page
	import="com.redhat.refarch.microservices.presentation.RestClient"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<div style="margin-top: 5em; margin-bottom: 1em;">
	<table style="margin: 0px auto;">
		<caption style="margin: 0px auto; font-size: 2em; padding: 1em;">Order
			Summary</caption>
		<tr style="font-weight: bold;">
			<td style="border: 1px solid black; padding: 5px">Product</td>
			<td style="border: 1px solid black; padding: 5px">Unit Price</td>
			<td style="border: 1px solid black; padding: 5px">Quantity</td>
			<td style="border: 1px solid black; padding: 5px">Product Cost</td>
		</tr>
		<c:set var="total" value="${0}" />
		<c:forEach var="orderItem" items="${orderItems}">
			<c:set var="product" value="${inventory[orderItem.sku]}" />
			<tr style="border: 1px solid black;">
				<td
					style="border: 1px solid black; padding: 5px; text-align: right;">${product.name}</td>
				<td
					style="border: 1px solid black; padding: 5px; text-align: right;"><fmt:formatNumber
						value="${product.price}" type="currency" groupingUsed="true" /></td>
				<td
					style="border: 1px solid black; padding: 5px; text-align: right;">${orderItem.quantity}</td>
				<td
					style="border: 1px solid black; padding: 5px; text-align: right;"><fmt:formatNumber
						value="${product.price * orderItem.quantity}" type="currency"
						groupingUsed="true" /></td>
			</tr>
			<c:set var="total"
				value="${total + product.price * orderItem.quantity}" />
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
	</table>
</div>
<form target="_self" method="post">
	<input type="hidden" name="amount" value="${total}">
	<table style="margin: 0em auto; border: 0px; padding: 2em;">
		<tr>
			<td style="padding: 5px;">Customer:</td>
			<td style="padding: 5px;">${sessionScope.customer.name}</td>
		</tr>
		<tr>
			<td style="padding: 5px;">Telephone:</td>
			<td style="padding: 5px;">${sessionScope.customer.telephone}</td>
		</tr>
		<tr>
			<td style="padding: 5px;">Address:</td>
			<td style="padding: 5px;">${sessionScope.customer.address}</td>
		</tr>
		<tr>
			<td style="padding: 5px;">Credit Card No:</td>
			<td style="padding: 5px;"><input type="text" name="creditCardNo"
				size="18" maxlength="16" pattern="\d{16}" required></td>
		</tr>
		<tr>
			<td style="padding: 5px;">Expiration Date</td>
			<td style="padding: 5px;"><select name='expirationMM'
				id='expirationMM'>
					<option value='01'>Janaury</option>
					<option value='02'>February</option>
					<option value='03'>March</option>
					<option value='04'>April</option>
					<option value='05'>May</option>
					<option value='06'>June</option>
					<option value='07'>July</option>
					<option value='08'>August</option>
					<option value='09'>September</option>
					<option value='10'>October</option>
					<option value='11'>November</option>
					<option value='12'>December</option>
			</select> <select name='expirationYY' id='expirationYY'>
					<option value='2015'>2015</option>
					<option value='2016'>2016</option>
					<option value='2017'>2017</option>
					<option value='2018'>2018</option>
					<option value='2019'>2019</option>
			</select></td>
		</tr>
		<tr>
			<td style="padding: 5px;">Verification Code</td>
			<td style="padding: 5px;"><input type="text"
				name="verificationCode" max="999" size="4" maxlength="3"
				pattern="\d{3}" required></td>
		</tr>
	</table>

	<div style="margin: 0px auto; text-align: center;">
		<button name="completeOrder" value="true"
			style="background-color: LightBlue; font-size: 1em; padding: 5px; margin-left: 20px; margin-right: 20px;">Submit</button>
		<button onclick="document.getElementById('cancel_form').submit();"
			type="button"
			style="background-color: LightBlue; font-size: 1em; padding: 5px; margin-left: 20px; margin-right: 20px;">Cancel</button>
	</div>
</form>

<form id="cancel_form" target="_self" method="post"></form>
