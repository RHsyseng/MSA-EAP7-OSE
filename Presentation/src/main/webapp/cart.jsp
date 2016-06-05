<%@page
	import="com.redhat.refarch.microservices.presentation.RestClient"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:if test="${param.updateQuantity}">
	<%
		RestClient.updateQuantity(request);
	%>
</c:if>
<form target="_self" id="returnForm" method="post">
	<table style="width: 100%;">
		<tr>
			<c:if test="${not empty errorMessage}">
				<td>
					<div style="color: red">${errorMessage}</div>
				</td>
			</c:if>
			<td style="float: right; border: 0; text-align: right;">
				<button name="home" id="home" value="true"
					style="margin-right: 20px; margin-left: 20px;">Return</button>
			</td>
		</tr>
	</table>
	<c:if test="${itemCount == 0}">
		<script type="text/javascript">
			document.getElementById('returnForm').submit();
		</script>
	</c:if>
</form>
<div style="margin-top: 5em;">
	<c:forEach var="orderItem" items="${orderItems}">
		<c:set var="product" value="${inventory[orderItem.sku]}" />
		<br />
		<br />
		<table style="margin: 0px auto; width: 80%; border: 1px solid black;">
			<caption style="margin: 0px auto; font-size: 2em">${product.name}</caption>
			<tr style="border: 1px solid black;">
				<td style="border: 1px solid black; padding: 5px"><img
					alt="${product.name}" src="/images/${product.image}.png"
					height="144" width="144"></td>
				<td style="border: 1px solid black; padding: 5px">${product.description}</td>
				<td style="border: 1px solid black; padding: 5px">Product
					Dimensions: ${product.length} x ${product.width} x
					${product.height} <br /> Product Weight: ${product.weight}
				</td>
				<td style="border: 1px solid black; padding: 5px">
					<p style="font-size: 1.5em">$${product.price}</p>
					<p>Availability: ${product.availability}</p>
					<form target="_self" method="post">
						<input type="hidden" name="cart" value="true"> <input
							type="hidden" name="orderItemId" value="${orderItem.id}"> <input
							type="number" name="quantity" size="5"
							value="${orderItem.quantity}">
						<button name="updateQuantity" id="updateQuantity" value="true"
							type="submit">Update</button>
						<button name="delete" type="button"
							onclick="deleteItem(this.form);">Delete</button>
					</form>
				</td>
			</tr>
		</table>
	</c:forEach>
</div>

<form target="_self" method="post">
	<table style="width: 100%; margin-top: 3em">
		<tr>
			<td style="text-align: center;">
				<button name="checkout" value="true"
					style="background-color: LightBlue; font-size: 1.5em; padding: 5px;">Checkout</button>
			</td>
		</tr>
	</table>
</form>

<script type="text/javascript">
	function deleteItem(itemForm) {
		itemForm.elements["quantity"].value = 0;
		itemForm.elements["updateQuantity"].click();
	}
</script>