<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Orders</title>
</head>
<body>
<h1>Orders</h1>
<#if order??>
    ${order}
<#else>
    No orders available.
</#if>
</body>
</html>
