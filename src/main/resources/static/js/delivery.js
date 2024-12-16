const Cities = [];

document.addEventListener("DOMContentLoaded", function () {
    fetch('/restaurant/cities')
        .then(response => response.json())
        .then(cities => {
            Cities.push(...cities);  // Записываем города в массив Cities
            displayCities(cities);    // Отображаем города в выпадающем списке
        })
        .catch(error => console.error('Error:', error));
});

function displayCities(cities) {
    const citySelect = document.getElementById("citySelect");
    citySelect.innerHTML = '<option value="" class="required-field">--Select city--</option>';

    cities.forEach(city => {
        const option = document.createElement("option");
        option.value = city.cityId;
        option.textContent = city.city;
        citySelect.appendChild(option);
    });
}

document.getElementById("citySelect").addEventListener("change", function () {
    const cityId = this.value;
    const streetSelect = document.getElementById("streetSelect");

    streetSelect.innerHTML = '<option value="" class="required-field">--Select street--</option>';

    if (!cityId) {
        streetSelect.disabled = true;
        return;
    }

    fetch(`/restaurant/streets?cityId=${cityId}`)
        .then(response => response.json())
        .then(data => {
            streetSelect.disabled = false;

            data.forEach(street => {
                const option = document.createElement("option");
                option.value = street.streetId;
                option.textContent = street.nameStreet;
                streetSelect.appendChild(option);
            });
        })
        .catch(error => console.error("Ошибка при получении улиц:", error));
});

// Функция для выбора способа оплаты
function selectPaymentMethod(method) {
    const paymentMethodElement = document.getElementById("paymentMethod");
    if (method === 'card') {
        paymentMethodElement.textContent = "Оплата картой";
    } else if (method === 'cash') {
        paymentMethodElement.textContent = "Оплата наличными";
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const deliveryOption = document.getElementById("delivery");
    const pickupOption = document.getElementById("pickup");
    const deliveryAddressSection = document.querySelector(".delivery-address");

    // Функция для переключения видимости блока "Адрес доставки"
    function toggleDeliveryAddress() {
        if (deliveryOption.checked) {
            deliveryAddressSection.style.display = "block"; // Показываем
        } else if (pickupOption.checked) {
            deliveryAddressSection.style.display = "none"; // Скрываем
        }
    }

    // Добавляем обработчики событий для изменения радио-кнопок
    deliveryOption.addEventListener("change", toggleDeliveryAddress);
    pickupOption.addEventListener("change", toggleDeliveryAddress);

    // Инициализация состояния при загрузке страницы
    toggleDeliveryAddress(); // Это вызовет отображение/скрытие в зависимости от выбранной опции
});