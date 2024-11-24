<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Orders</title>
</head>
<body>
<h1>Orders</h1>

<#if Error??>
    <p>Ошибка: ${Error}</p>
</#if>

<!-- Отображение id ресторана -->
<#if idRestaurant??>
    <p>id Restaurant: ${idRestaurant}</p>
<#else>
    <p>id Restaurant: Не доступно</p>
</#if>

<!-- Форма для изменения apiLogin -->
<form action="/admin/updateApiLogin" method="post">
    <label for="apiLogin">API Login:</label>
    <#if apiLogin??>
        <input type="text" id="apiLogin" name="apiLogin" value="${apiLogin}" required />
    <#else>
        <input type="text" id="apiLogin" name="apiLogin" placeholder="Введите логин" required />
    </#if>

    <button type="submit">Отправить</button>
</form>

<!-- Отображение имени ресторана -->
<#if nameRestaurant??>
    <p>Name Restaurant: ${nameRestaurant}</p>
<#else>
    <p>Name Restaurant: Не доступно</p>
</#if>


<form action="/admin/resetToken" method="post">
    <button type="submit">Сбросить токен</button>
</form>

</body>
</html>
