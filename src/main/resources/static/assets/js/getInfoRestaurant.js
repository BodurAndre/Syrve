$(document).ready(function () {
    // Функция для загрузки текущей информации о ресторане
    function loadRestaurantInfo() {
        $.ajax({
            url: '/admin/viewRestaurant',
            method: 'GET',
            success: function (data) {
                if (data) {
                    // Обновляем интерфейс с новыми данными
                    $('#nameRestaurant').val(data.nameRestaurant);
                    $('#idRestaurant').val(data.idRestaurant);
                    $('#apiLogin').val(data.apiLogin);
                    $('#phoneRestaurant').val(data.phoneRestaurant);
                    $('#emailRestaurant').val(data.emailRestaurant);
                    $('#addressRestaurant').val(data.addressRestaurant);
                    $('.restaurant-name').val(data.nameRestaurant);
                }
            },
            error: function () {
                showNotification('Ошибка при загрузке информации о ресторане.', false);
            }
        });
    }

    // Загрузка информации о ресторане при загрузке страницы
    loadRestaurantInfo();

    // Сохранение нового API Login
    $('#saveApiLogin').on('click', function () {
        const newApiLogin = $('#apiLogin').val(); // Получаем новое значение
        const emailRestaurant = $('#emailRestaurant').val();
        const phoneRestaurant = $('#phoneRestaurant').val();
        const addressRestaurant = $('#addressRestaurant').val();

        if (!newApiLogin) {
            showNotification('Поле API Login не может быть пустым', false);
            return;
        }

        $.ajax({
            url: '/admin/updateApiLogin',
            method: 'POST',
            contentType: 'application/x-www-form-urlencoded',
            data: { apiLogin: newApiLogin,
                    emailRestaurant: emailRestaurant,
                    phoneRestaurant: phoneRestaurant,
                    addressRestaurant: addressRestaurant}, // Отправляем данные
            success: function () {
                $.ajax({
                    url: '/admin/resetToken',
                    method: 'POST',
                    success: function () {
                        $.ajax({
                            url: '/getOrganization',
                            method: 'GET',
                            success: function () {
                                showNotification('API Login успешно обновлен!', true);
                                loadRestaurantInfo();
                            },
                            error: function (response) {
                                loadRestaurantInfo();
                                showNotification(response.responseText, false);
                            }
                        });
                    },
                    error: function (response) {
                        loadRestaurantInfo();
                        showNotification(response.responseText, false);
                    }
                });
            },
            error: function (response) {
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
                loadRestaurantInfo();
            },
            error: function (response) {
                console.log(response);
                showNotification(response.responseText, false);
            }
        });
    });

    $('#getAddress').on('click', function () {
        $.ajax({
            url: '/getOrganization',
            method: 'GET',
            success: function () {
                showNotification('Токен успешно сброшен!', true);
                // Обновляем информацию о ресторане без перезагрузки страницы
                loadRestaurantInfo();
            },
            error: function (response) {
                console.log(response);
                showNotification(response.responseText, false);
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
