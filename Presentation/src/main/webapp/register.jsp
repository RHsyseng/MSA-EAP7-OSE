<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<form target="_self" method="post">
	<table style="margin: 0px auto; border: 1px solid black;">
		<caption style="margin: 0px auto; font-size: 2em">Customer
			Registration</caption>
		<tr style="border: 1px solid black;">
			<td style="border: 1px solid black; padding: 5px; min-width: 8em;">Name:</td>
			<td style="border: 1px solid black; padding: 5px;"><input
				name="name" type="text" size="30" /></td>
		</tr>
		<tr style="border: 1px solid black;">
			<td style="border: 1px solid black; padding: 5px; min-width: 8em;">Address:</td>
			<td style="border: 1px solid black; padding: 5px;"><input
				name="address" type="text" size="30" /></td>
		</tr>
		<tr style="border: 1px solid black;">
			<td style="border: 1px solid black; padding: 5px; min-width: 8em;">Telephone:</td>
			<td style="border: 1px solid black; padding: 5px;"><input
				name="telephone" type="text" size="30" /></td>
		</tr>
		<tr style="border: 1px solid black;">
			<td style="border: 1px solid black; padding: 5px; min-width: 8em;">Email:</td>
			<td style="border: 1px solid black; padding: 5px;"><input
				name="email" type="text" size="30" /></td>
		</tr>
		<tr style="border: 1px solid black;">
			<td style="border: 1px solid black; padding: 5px; min-width: 8em;">Username:</td>
			<td style="border: 1px solid black; padding: 5px;"><input
				name="username" type="text" size="30" /></td>
		</tr>
		<tr style="border: 1px solid black;">
			<td style="border: 1px solid black; padding: 5px; min-width: 8em;">Password:</td>
			<td style="border: 1px solid black; padding: 5px;"><input
				name="password" type="password" size="30" /></td>
		</tr>
	</table>
	<div style="margin: 10px auto; width: 100%; text-align: center;">
		<button name="registration" value="true">Register</button>
		<button name="registration" value="false">Cancel</button>
	</div>
</form>
