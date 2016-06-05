<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<form id="headerForm" target="_self" method="post">
	<table style="width: 100%;">
		<tr>
			<c:if test="${not empty successMessage}">
				<td>
					<div style="color: green">${successMessage}</div>
				</td>
			</c:if>
			<c:if test="${not empty errorMessage}">
				<td>
					<div style="color: red">${errorMessage}</div>
				</td>
			</c:if>
			<c:if test="${not empty sessionScope.customer}">
				<td>
					<table style="float: right; border: 0; text-align: right;">
						<tr>
							<td style="margin-right: 20px;">Welcome back,
								${customer.name}</td>
							<td style="padding-right: 20px; padding-left: 20px;"><a
								href="javascript:'" onclick="history();">Order History</a><input
								type="hidden" id="history" name="history" /></td>
							<td>
								<button name="logout" value="true"
									style="margin-right: 20px; margin-left: 20px;">Log Out</button>
							</td>
							<c:if test="${itemCount > 0}">
								<td><button name="cart" id="cart" value="true"
										style="visibility: hidden;"></button></td>
								<td
									style="margin-right: 10px; display: block; position: relative;">
									<img style="opacity: 0.6;" alt="Shopping Cart"
									onclick="clickCart();" src="/images/shopping-cart.png"
									height="36" width="36" />
									<p style="opacity: 1; position: absolute; top: 0; left: 15px;"
										onclick="clickCart();">
										<c:out value="${itemCount}" />
									</p>
								</td>
							</c:if>
							<c:if test="${itemCount == 0}">
								<td
									style="margin-right: 10px; display: block; position: relative;">
									<img style="opacity: 01;" alt="Shopping Cart"
									src="/images/shopping-cart.png" height="36" width="36" />
								</td>
							</c:if>
						</tr>
					</table>
				</td>
			</c:if>
			<c:if test="${empty sessionScope.customer}">
				<td style="float: right; text-align: right;">
					<div>
						<input type="text" name="username" style="margin-right: 5px;" />
						<input type="password" name="password" style="margin-right: 5px;" />
						<button style="margin-right: 10px;" name="login" value="true">Login</button>
						<button style="margin-right: 5px;" name="register" value="true">
							Register</button>
					</div>
				</td>
			</c:if>
		</tr>
	</table>
</form>

<script type="text/javascript">
	function history() {
		document.getElementById('history').value = true;
		document.getElementById("headerForm").submit();
	}
	function clickCart() {
		document.getElementById('cart').click();
	}
</script>