isModel = false;
statusResponse = true;

$(document).ready(function () {
    const table = $('.datanew').DataTable();

    function loadCitiesWithStreets() {
        table.clear();

        $.ajax({
            url: '/admin/viewOrder',
            method: 'GET',
            success: function (data) {
                data.forEach(order => {
                    const row = [
                        '<label class="checkboxs"><input type="checkbox"><span class="checkmarks"></span></label>',
                        order.id,
                        order.data,
                        order.phone,
                        `${order.status === "Pending" ? `<span class="badges bg-lightred">${order.status}</span>` : ''}
                         ${order.status === "Completed" ? `<span class="badges bg-lightgreen">${order.status}</span>` : ''}
                         ${order.status === "Waiting" ? `<span class="badges bg-lightyellow">${order.status}</span>` : ''}
                         ${order.status === "ERROR" ? `<span class="badges bg-lightred">${order.status}</span>` : ''}`,
                        order.payment ? '<span class="badges bg-lightgreen">Paid</span>' : '<span class="badges bg-lightred">Due</span>',
                        ` ${order.total} lei`,
                        `<div class="text-center">
            <a class="action-set" href="javascript:void(0);" data-bs-toggle="dropdown" aria-expanded="true">
                <i class="fa fa-ellipsis-v" aria-hidden="true"></i>
            </a>
            <ul class="dropdown-menu">
                <li>
                    <a href="/admin/order/${order.id}" class="dropdown-item">
                    <img src="/assets/img/icons/eye1.svg" class="me-2" alt="img">Sale Detail
                    </a>
                </li>
                        ${order.status === "Pending" || order.status === "ERROR"
                            ? `<li>
                        <a href="javascript:void(0);" class="dropdown-item setOrder" data-id="${order.id}">
                            <img src="/assets/img/icons/download.svg" class="me-2" alt="img">Checkout
                        </a>
                    </li>`
                            : ''}
                <li>
                    <a href="javascript:void(0);" class="dropdown-item confirm-text">
                        <img src="/assets/img/icons/delete1.svg" class="me-2" alt="img">Delete Sale
                    </a>
                </li>
            </ul>
        </div>`
                    ];
                    table.row.add(row).draw();
                });

            },
            error: function () {
                alert('Ошибка при загрузке данных.');
            }
        });

    }
    // Загрузка данных при загрузке страницы
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

    if(isModel) {
        if(statusResponse) showNotification('Города и улицы успешно обновлены', true);
        else showNotification('Произошла ошибка при обновлении данных', false);
    }

    $('#getAddress').on('click', function (event) {
        // Предотвращаем стандартное поведение (например, отправка формы)
        event.preventDefault();
        $.ajax({
            url: '/admin/saveAddress',
            method: 'GET',
            success: function () {
                isModel = true;
                // Предполагаем, что сервер возвращает строку или JSON-объект с сообщением
                statusResponse = true;
                showNotification('Заказы успешно обновлены', true); // Добавлено вызов уведомления
                loadCitiesWithStreets();
            },
            error: function (xhr) {
                // Обрабатываем сообщение об ошибке от сервера, если оно передается
                isModel = true;
                statusResponse = false;
                showNotification('Произошла ошибка при обновлении данных', false); // Добавлено вызов уведомления
                loadCitiesWithStreets();
            }
        });
    });

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

        if (!selectedOrderId) {
            alert('Ошибка: не найден ID заказа.');
            return;
        }
        console.log("restaurantId - " + restaurantId);
        console.log("selectedOrderId - " + selectedOrderId);
        //Сначала обновим статус
        $.ajax({
            url: '/admin/editStatus',
            method: 'POST',
            contentType: 'application/json',
            data: `${selectedOrderId}`,
            success: function () {
                loadCitiesWithStreets();
                $('#orderModal').removeClass('show').fadeOut();
                // Потом отправим заказ с ID ресторана
                $.ajax({
                    url: '/ordering',
                    method: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify({
                        orderId: String(selectedOrderId),
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




    $(document).on('click', '.checkOutOrder', function () {
        const orderId = $(this).data('id');

        $.ajax({
            url: '/admin/editStatus',
            method: 'POST',
            contentType: 'application/x-www-form-urlencoded',
            data: JSON.stringify(orderId), // Отправляем данные
            success: function () {
                loadCitiesWithStreets();

                $.ajax({
                    url: '/ordering',
                    method: 'POST',
                    contentType: 'application/x-www-form-urlencoded',
                    data: JSON.stringify(orderId), // Отправляем данные
                    success: function (response) {
                        console.log(response);
                        showNotification('Заказ отправлен на кассу', true);
                        loadCitiesWithStreets();
                    },
                    error: function (response) {
                        console.error('Error:', response);
                        showNotification("Заказ ID "+ orderId + " не доставлен, причина: " + response.responseText, false);
                        loadCitiesWithStreets();
                    }
                });
            },
            error: function (response) {
                showNotification("Ошибка в изменения статуса"+ response.responseText, false);
                loadCitiesWithStreets();
            }
        });
    });

    $(document).on('click', '.getOrder', function () {
        $.ajax({
            url: '/getOrder',
            method: 'POST',
            contentType: 'application/x-www-form-urlencoded',
            data: JSON.stringify(orderId), // Отправляем данные
            success: function (response) {
                console.log(response);
                loadCitiesWithStreets();
            },
            error: function (response) {
                console.error('Error:', response);
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
