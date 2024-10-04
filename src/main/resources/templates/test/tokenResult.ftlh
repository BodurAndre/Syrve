<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Token Result</title>
</head>
<body>
<h2>Token Result</h2>
<p>
    <#if Error??>
        ${Error} <br>
    </#if>
    <#if body??>
        ${body} <br>
    </#if>
</p>
</body>
</html>
