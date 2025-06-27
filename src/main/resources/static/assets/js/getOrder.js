isModel = false;
statusResponse = true;

const orderId = window.location.pathname.split("/").pop();
$(document).ready(function () {

    function loadCitiesWithStreets() {

        const orderDetails = document.getElementById("orderDetails");
        const customerInfo = document.getElementById("customerInfo");
        const addressInfo = document.getElementById("addressInfo");
        const dishesTable = document.getElementById("dishesTable");

        fetch(`/admin/api/order/${orderId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error("Order not found");
                }
                return response.json();
            })
            .then(order => {

                const setOrderButton = document.querySelector('.setOrder');

                // Проверяем статус и управляем активностью кнопки
                if (order.status === 'Pending' || order.status === 'ERROR') {
                    setOrderButton.classList.remove('disabled'); // Активируем кнопку
                    setOrderButton.style.pointerEvents = 'auto'; // Разрешаем клик
                    setOrderButton.style.opacity = '1'; // Визуально делаем кнопку активной
                } else if (order.status === 'Completed' || order.status === 'Waiting') {
                    setOrderButton.classList.add('disabled'); // Деактивируем кнопку
                    setOrderButton.style.pointerEvents = 'none'; // Запрещаем клик
                    setOrderButton.style.opacity = '0.5'; // Визуально делаем кнопку неактивной
                }

                const totalElement = document.getElementById("total");
                if (totalElement) {
                    totalElement.textContent = `${order.total} MDL`; // Добавляем "MDL" для валюты
                }

                // Заполняем информацию о заказе
                orderDetails.innerHTML = `
                    <font style="vertical-align: inherit;">
                        <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">ID: ${order.id}</font>
                    </font><br>
                    <font style="vertical-align: inherit;">
                        <font style="vertical-align: inherit;font-size: 14px;color:${order.payment ? '#2E7D32' : '#FF2400'};font-weight: 400;">
                            ${order.payment ? 'Paid' : 'Not Paid'}
                        </font>
                    </font><br>
                    <font style="vertical-align: inherit;">
                        <font style="vertical-align: inherit;font-size: 14px;color:${order.status === 'Completed' ? '#2E7D32' : '#FF2400'};font-weight: 400;">
                            ${order.status}
                        </font>
                    </font><br>
                `;

                // Заполняем информацию о клиенте
                customerInfo.innerHTML = `
                    <font style="vertical-align: inherit;">
                        <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">Name: ${order.customer?.name || 'N/A'}</font>
                    </font><br>
                    <font style="vertical-align: inherit;">
                        <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">Email: ${order.customer?.email || 'N/A'}</font>
                    </font><br>
                    <font style="vertical-align: inherit;">
                        <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">Phone: ${order.customer?.phone || 'N/A'}</font>
                    </font><br>
                `;

                // Заполняем адрес
                addressInfo.innerHTML = order.address
                    ? `
                        <font style="vertical-align: inherit;">
                            <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">City: ${order.address.city}</font>
                        </font><br>
                        <font style="vertical-align: inherit;">
                            <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">Street: ${order.address.street}</font>
                        </font><br>
                        <font style="vertical-align: inherit;">
                            <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">House: ${order.address.house}</font>
                        </font><br>
                    `
                    : `<font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">Pickup</font><br>`;

                // Заполняем таблицу блюд
                order.dishes.forEach(dish => {
                    const row = document.createElement("tr");
                    row.classList.add("details");
                    row.style.borderBottom = "1px solid #E9ECEF";

                    row.innerHTML = `
                        <td style="padding: 10px;vertical-align: top; display: flex;align-items: center;">
                            <img src="${dish.imageLinks}" alt="${dish.name}" class="me-2" style="width:40px;height:40px;">
                            ${dish.name}
                        </td>
                        <td style="padding: 10px;vertical-align: top;">
                            ${dish.amount}
                        </td>
                        <td style="padding: 10px;vertical-align: top;">
                            ${dish.price}
                        </td>
                        <td style="padding: 10px;vertical-align: top;">
                            ${dish.discount}
                        </td>
                        <td style="padding: 10px;vertical-align: top;">
                            ${dish.subtotal}
                        </td>
                    `;
                    dishesTable.appendChild(row);
                });
            })
            .catch(error => {
                console.error(error);
                alert("Не удалось загрузить данные заказа.");
            });
    }

    loadCitiesWithStreets();

    function loadRestaurantInfo() {
        $.ajax({
            url: '/admin/viewRestaurant',
            method: 'GET',
            success: function (data) {
                console.log(data);
                const select = $('#orderOptions');
                select.empty(); // очищаем старые опции

                // Добавляем дефолтный option
                select.append('<option value="" disabled selected>Select restaurant</option>');

                if (Array.isArray(data)) {
                    data.forEach(terminal => {
                        const option = $('<option>', {
                            value: terminal.terminalId, // или terminal.terminalId, если тебе нужен именно он
                            text: terminal.nameRestaurant + ' - ' + terminal.address
                        });
                        select.append(option);
                    });
                } else {
                    showNotification('Данные о терминалах не найдены.', false);
                }
            },
            error: function () {
                showNotification('Ошибка при загрузке информации о ресторане.', false);
            }
        });

    }

    loadRestaurantInfo();

    if (isModel) {
        showNotification(
            statusResponse ? 'Города и улицы успешно обновлены' : 'Произошла ошибка при обновлении данных',
            statusResponse
        );
    }


    let selectedOrderId = null;

    $(document).on('click', '.setOrder', function () {
        selectedOrderId = $(this).data('id');
        $('#orderModal').addClass('show').fadeIn();
    });

    $('#cancelCheckout').on('click', function () {
        $('#orderModal').removeClass('show').fadeOut();
    });


    $('#confirmCheckout').on('click', function () {
        const restaurantId = $('#orderOptions').val();
        if (!restaurantId) {
            alert('Пожалуйста, выберите ресторан!');
            return;
        }
        console.log("restaurantId - " + restaurantId);
        console.log("selectedOrderId - " + orderId);
        //Сначала обновим статус
        $.ajax({
            url: '/admin/editStatus',
            method: 'POST',
            contentType: 'application/json',
            data: `${orderId}`,
            success: function () {
                loadCitiesWithStreets();
                $('#orderModal').removeClass('show').fadeOut();
                // Потом отправим заказ с ID ресторана
                $.ajax({
                    url: '/ordering',
                    method: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify({
                        orderId: String(orderId),
                        restaurantId: String(restaurantId)
                    }), // передаём restaurantId
                    success: function (response) {
                        console.log(response);
                        showNotification('Заказ отправлен в ресторан', true);
                        $('#orderModal').removeClass('show').fadeOut();
                        loadCitiesWithStreets();
                    },
                    error: function (response) {
                        console.error('Ошибка:', response);
                        showNotification("Заказ ID " + selectedOrderId + " не отправлен: " + response.responseText, false);
                        $('#orderModal').removeClass('show').fadeOut();
                        loadCitiesWithStreets();
                    }
                });
            },
            error: function (response) {
                showNotification("Ошибка при изменении статуса: " + response.responseText, false);
                $('#orderModal').removeClass('show').fadeOut();
                loadCitiesWithStreets();
            }
        });
    });

});

function showNotification(message, isSuccess = false) {
    isModel = false;

    // Удаляем существующее уведомление, если оно есть
    const existingNotification = document.querySelector('.custom-notification');
    if (existingNotification) {
        existingNotification.remove();
    }

    // Создаем элемент уведомления
    const notification = document.createElement('div');
    notification.classList.add('custom-notification');
    notification.textContent = message;

    // Устанавливаем начальные стили
    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '60px';
    notification.style.backgroundColor = isSuccess ? '#4CAF50' : '#f44336'; // Зеленый для успеха, красный для ошибки
    notification.style.color = 'white';
    notification.style.padding = '10px 20px';
    notification.style.borderRadius = '5px';
    notification.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
    notification.style.fontSize = '16px';
    notification.style.zIndex = '9999'; // Убедитесь, что окно сверху
    notification.style.opacity = '0'; // Начальное состояние (прозрачное)
    notification.style.transform = 'scale(0.9)'; // Начальное состояние (уменьшенное)
    notification.style.transition = 'opacity 0.5s, transform 0.5s'; // Анимации для появления и исчезновения

    // Добавляем уведомление в body
    document.body.appendChild(notification);

    // Появление (анимация)
    setTimeout(() => {
        notification.style.opacity = '1';
        notification.style.transform = 'scale(1)';
    }, 10); // Небольшая задержка для срабатывания CSS-транзишена

    // Автоматическое исчезновение
    setTimeout(() => {
        notification.style.opacity = '0';
        notification.style.transform = 'scale(0.9)'; // Уменьшение при исчезновении
        setTimeout(() => {
            notification.remove();
        }, 500); // Удаление после анимации исчезновения
    }, 5000);
}