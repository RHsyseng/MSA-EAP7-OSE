<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div style="margin-top: 5em;"></div>
<form target="_self" method="post" style="margin: 0px auto;">
	<table style="margin: 0px auto; width: 30em; border: 0px;">
		<tr>
			<td><input type="text" name="query" size="50">
				<button type="submit">Search</button></td>
		</tr>
	</table>
</form>
<c:forEach var="product" items="${products}">
	<br />
	<br />
	<table style="margin: 0px auto; width: 80%; border: 1px solid black;">
		<caption style="margin: 0px auto; font-size: 3em">${product.name}</caption>
		<tr style="border: 1px solid black;">
			<td style="border: 1px solid black; padding: 5px"><img
				alt="${product.name}" src="/images/${product.image}.png"
				height="144" width="144"></td>
			<td style="border: 1px solid black; padding: 5px">${product.description}</td>
			<td style="border: 1px solid black; padding: 5px">Product
				Dimensions: ${product.length} x ${product.width} x ${product.height}
				<br /> Product Weight: ${product.weight}
			</td>
			<td style="border: 1px solid black; padding: 5px">
				<p style="font-size: 1.5em">$${product.price}</p>
				<p>Availability: ${product.availability}</p> <c:if
					test="${sessionScope.customer != null}">
					<form target="_self" method="post">
						<input type="hidden" name="sku" value="${product.sku}">
						<button name="purchase" value="true" type="submit">Purchase</button>
					</form>
				</c:if>
			</td>
		</tr>
	</table>
</c:forEach>
