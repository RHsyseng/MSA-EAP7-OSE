<%@page import="com.redhat.refarch.microservices.presentation.Demo"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Products</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<head>
<body>
	<%
		Demo.populate();
		response.sendRedirect( request.getContextPath() );
	%>
</body>
</html>