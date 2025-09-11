$(document).ready(function () {
    // Функция для загрузки текущей информации о ресторане
    function loadCompanyInfo() {
        $.ajax({
            url: '/admin/viewCompany',
            method: 'GET',
            success: function (data) {
                if (data) {
                    // Обновляем интерфейс с новыми данными
                    console.log(data)
                    $('#nameCompany').val(data.nameRestaurant || '');
                    $('#idCompany').val(data.idRestaurant || '');
                    $('#apiLogin').val(data.apiLogin || '');
                    $('#phoneCompany').val(data.phoneRestaurant || '');
                    $('#emailCompany').val(data.emailRestaurant || '');
                    $('#sectorRestaurant').val(data.sectorRestaurant || '').trigger('change');
                    $('#typeRestaurant').val(data.typeRestaurant || '').trigger('change');
                    $('.restaurant-name').val(data.nameRestaurant || '');
                }
            },
            error: function () {
                showNotification('Ошибка при загрузке информации о ресторане.', false);
            }
        });
    }

    function loadRestaurantInfo() {
        $.ajax({
            url: '/admin/viewRestaurant',
            method: 'GET',
            success: function (data) {
                const container = $('#terminalCardsContainer'); // обёртка, куда вставлять карточки
                container.empty(); // очищаем перед вставкой новых

                if (Array.isArray(data)) {
                    data.forEach(terminal => {
                        const card = `
                        <div class="card mb-3">
                            <div class="card-body">
                                <div class="row">
                                    <div class="col-lg-5 col-sm-6 col-12">
                                        <div class="form-group">
                                            <label>Name Restaurant</label>
                                            <input type="text" value="${terminal.nameRestaurant}" disabled>
                                        </div>
                                    </div>
                                    <div class="col-lg-5 col-sm-6 col-12">
                                        <div class="form-group">
                                            <label>Restaurant ID</label>
                                            <input type="text" value="${terminal.terminalId}" disabled>
                                        </div>
                                    </div>
                                    <div class="col-lg-5 col-sm-6 col-12">
                                        <div class="form-group">
                                            <label>Address</label>
                                            <input type="text" value="${terminal.address}">
                                        </div>
                                    </div>
                                    <div class="col-lg-5 col-sm-6 col-12">
                                        <div class="form-group">
                                            <label>Time zone</label>
                                            <input type="text" value="${terminal.timeZone}">
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    `;
                        container.append(card); // добавляем в DOM
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
    loadCompanyInfo();
    // Загрузка информации о ресторане при загрузке страницы
    loadRestaurantInfo();

    // Сохранение нового API Login
    $('#saveApiLogin').on('click', function () {
        const newApiLogin = $('#apiLogin').val(); // Получаем новое значение
        const emailCompany = $('#emailCompany').val();
        const phoneCompany = $('#phoneCompany').val();
        const addressRestaurant = $('#addressRestaurant').val();
        const sectorRestaurant = $('#sectorRestaurant').val();
        const typeRestaurant = $('#typeRestaurant').val();

        if (!newApiLogin) {
            showNotification('Поле API Login не может быть пустым', false);
            return;
        }

        $.ajax({
            url: '/admin/updateApiLogin',
            method: 'POST',
            contentType: 'application/x-www-form-urlencoded',
            data: { apiLogin: newApiLogin,
                    emailRestaurant: emailCompany,
                    phoneRestaurant: phoneCompany,
                    addressRestaurant: addressRestaurant,
                    sectorRestaurant: sectorRestaurant,
            typeRestaurant: typeRestaurant},// Отправляем данные
            success: function () {
                $.ajax({
                    url: '/admin/resetToken',
                    method: 'POST',
                    success: function () {
                        $.ajax({
                            url: '/admin/getToken',
                            method: 'GET',
                            success: function () {
                                $.ajax({
                                    url: '/getOrganization',
                                    method: 'GET',
                                    success: function () {
                                        $.ajax({
                                            url: '/admin/getTerminalGroup',
                                            method: 'GET',
                                            success: function (response) {
                                                showNotification('API Login успешно обновлен!', true);
                                                loadCompanyInfo();
                                                loadRestaurantInfo();
                                            },
                                            error: function (xhr) {
                                                const errorMsg = xhr.responseText || 'Произошла неизвестная ошибка.';
                                                showNotification(errorMsg, false);
                                                console.error("Ошибка при запросе терминальных групп:", errorMsg);
                                            }
                                        });
                                    },
                                    error: function (response) {
                                        loadCompanyInfo();
                                        showNotification(response.responseText, false);
                                    }
                                });
                            },
                            error: function (response) {
                                loadCompanyInfo();
                                showNotification(response.responseText, false);
                            }
                        });
                    },
                    error: function (response) {
                        loadCompanyInfo();
                        showNotification(response.responseText, false);
                    }
                });
            },
            error: function (response) {
                loadCompanyInfo();
                showNotification(response.responseText, false);
            }
        });
    });

    // Сброс токена
    $('#resetToken').on('click', function () {
        $.ajax({
            url: '/admin/resetToken',
            method: 'POST',
            success: function () {
                showNotification('Токен успешно сброшен!', true);
                // Обновляем информацию о ресторане без перезагрузки страницы
                loadCompanyInfo();
            },
            error: function (response) {
                console.log(response);
                showNotification(response.responseText, false);
            }
        });
    });

    $('#getAllInformation').on('click', function () {
        $.ajax({
            url: '/admin/getTerminalGroup',
            method: 'GET',
            success: function (response) {
                showNotification(response, true);
                loadRestaurantInfo();
            },
            error: function (xhr) {
                const errorMsg = xhr.responseText || 'Произошла неизвестная ошибка.';
                showNotification(errorMsg, false);
                console.error("Ошибка при запросе терминальных групп:", errorMsg);
            }
        });
    });

});


function showNotification(message, isSuccess = false) {
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
    notification.style.top = '40px';
    notification.style.right = '20px';
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
