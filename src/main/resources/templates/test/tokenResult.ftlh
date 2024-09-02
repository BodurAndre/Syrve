<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Token Result</title>
</head>
<body>
<h2>Token Result</h2>
<p>
    <#if body??>
        ${body} <br>
    </#if>
</p>
<#if products??>
    <h1>Товары</h1>
    <#list products as product>
        <div class="product">
            <h2>${product.name}</h2>
            <p>ID: ${product.id}</p>
            <p>Group ID: ${product.groupId}</p>
            <p>Code: ${product.code}</p>
            <p>Current price: ${product.currentPrice}</p>
            <p>Product id: ${product.productId}</p>
        </div>
    </#list>
</#if>
<#if dishes??>
    <h1>Блюда</h1>
    <#list dishes as dishWithModifiers>
        <h2>${dishWithModifiers.dish.name} (Code: ${dishWithModifiers.dish.code}, Price: ${dishWithModifiers.dish.currentPrice})</h2>
        <h3>Modifiers:</h3>
        <ul>
            <#list dishWithModifiers.dishModifiers as dishModifier>
                <li>${dishModifier.modifier.name} (Min: ${dishModifier.minQuantity}, Max: ${dishModifier.maxQuantity}, Default: ${dishModifier.quantity})</li>
            </#list>
        </ul>
    </#list>
</#if>
</body>
</html>
