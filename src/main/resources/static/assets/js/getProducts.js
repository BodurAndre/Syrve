isModel = false;
statusResponse = true;

$(document).ready(function () {
    function loadProducts() {
        Promise.all([
            fetch('/menu/dishes').then(response => response.json()),
            fetch('/menu/products').then(response => response.json())
        ])
            .then(([dishes, products]) => {
                displayDishesInTable(dishes, products);
            })
            .catch(error => console.error('Error:', error));
    }

    loadProducts()

    if(isModel) {
        if(statusResponse) showNotification('Города и улицы успешно обновлены', true);
        else showNotification('Произошла ошибка при обновлении данных', false);
    }

    $('#getProducts').on('click', function (event) {
        // Предотвращаем стандартное поведение (например, отправка формы)
        event.preventDefault();

        $.ajax({
            url: '/admin/saveProducts',
            method: 'GET',
            success: function () {
                isModel = true;
                // Предполагаем, что сервер возвращает строку или JSON-объект с сообщением
                statusResponse = true;
                showNotification("Номенклатура успешно обновлена", true); // Добавлено вызов уведомления
                loadProducts();
            },
            error: function (response) {
                isModel = true;
                statusResponse = false;
                showNotification(response.responseText, false); // Добавлено вызов уведомления
                loadProducts();
            }
        });
    });
});


function displayDishesInTable(dishes, products) {
    const table = $('.datanew').DataTable();

    table.clear();
    dishes.forEach(dish => {
        const row = [
            `<label class="checkboxs">
                <input type="checkbox">
                <span class="checkmarks"></span>
            </label>`,
            `<a href="javascript:void(0);" class="product-img">
                <img src="${dish.imageLinks || '/assets/img/product/noimage.png'}">
            </a>
            <a href="javascript:void(0);">${dish.name}</a>`,
            dish.groupName || 'N/A',
            `${dish.price} MDL`,
            dish.hasModifiers ? 'YES' : 'NO',
            `${dish.weight} gr`,
            dish.isIncludedMenu ? 'YES' : 'NO',
            `
            <a class="me-3" href="product-details.html" title="View Details">
                <img src="/assets/img/icons/eye.svg" alt="View">
            </a>
            <a class="me-3" href="editproduct.html" title="Edit Product">
                <img src="/assets/img/icons/edit.svg" alt="Edit">
            </a>
            <a class="confirm-text" href="javascript:void(0);" title="Delete Product">
                <img src="/assets/img/icons/delete.svg" alt="Delete">
            </a>
            `
        ];
        table.row.add(row).draw();
    });

    products.forEach(dish => {
        const row = [
            `<label class="checkboxs">
                <input type="checkbox">
                <span class="checkmarks"></span>
            </label>`,
            `<a href="javascript:void(0);" class="product-img">
                <img src="${dish.imageLinks || '/assets/img/product/noimage.png'}">
            </a>
            <a href="javascript:void(0);">${dish.name}</a>`,
            dish.groupName || 'N/A',
            `${dish.price} MDL`,
            dish.hasModifiers ? 'YES' : 'NO',
            `${dish.weight}`,
            dish.isIncludedMenu ? 'YES' : 'NO',
            `
            <a class="me-3" href="product-details.html" title="View Details">
                <img src="/assets/img/icons/eye.svg" alt="View">
            </a>
            <a class="me-3" href="editproduct.html" title="Edit Product">
                <img src="/assets/img/icons/edit.svg" alt="Edit">
            </a>
            <a class="confirm-text" href="javascript:void(0);" title="Delete Product">
                <img src="/assets/img/icons/delete.svg" alt="Delete">
            </a>
            `
        ];
        table.row.add(row).draw();
    });
}

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

