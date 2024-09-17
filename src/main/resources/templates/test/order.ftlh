<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Orders</title>
</head>
<body>
<h1>Orders</h1>
<table>
    <thead>
    <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Amount</th>
        <th>Code</th>
        <th>Modifiers</th>
    </tr>
    </thead>
    <tbody>
    <#if order??>
        ${order}
    <#else>
        No orders available.
    </#if>

</table>
</body>
</html>
