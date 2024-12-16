document.addEventListener('DOMContentLoaded', () => {
    // Получение строки с API
    fetch('/restaurant/getNameRestaurant')
        .then(response => {
            if (!response.ok) {
                throw new Error('Сетевая ошибка: ' + response.status);
            }
            return response.text();  // Получение данных как текст
        })
        .then(data => {
            // Привязка к переменной
            const restaurantName = data;

            // Поиск всех элементов с классом restaurant-name
            const elements = document.querySelectorAll('.restaurant-name');

            // Обновление содержимого каждого элемента
            elements.forEach(element => {
                element.textContent = restaurantName;
            });
        })
        .catch(error => {
            console.error('Ошибка:', error);

            // В случае ошибки обновить текст всех элементов с ошибочным сообщением
            const elements = document.querySelectorAll('.restaurant-name');
            elements.forEach(element => {
                element.textContent = 'ОШИБКА';
            });
        });
});
