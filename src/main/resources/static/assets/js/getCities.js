isModel = false;
statusResponse = true;

$(document).ready(function () {

    const table = $('.datanew').DataTable();

    function loadCitiesWithStreets() {
        $.ajax({
            url: '/api/locations/all',
            method: 'GET',
            success: function (data) {
                data.forEach(city => {
                    city.streets.forEach(street => {
                        const row = [
                            '<label class="checkboxs"><input type="checkbox"><span class="checkmarks"></span></label>',
                            city.cityName,
                            street.streetName,
                            street.isDeleted ? 'deleted' : 'active'
                        ];
                        table.row.add(row).draw();
                    });
                });
            },
            error: function () {
                alert('Ошибка при загрузке данных.');
            }
        });
    }

    // Загрузка данных при загрузке страницы
    loadCitiesWithStreets();

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
                showNotification('Города и улицы успешно обновлены', true); // Добавлено вызов уведомления
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
