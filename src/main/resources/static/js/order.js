// Читаем данные из localStorage
const storedCart = JSON.parse(localStorage.getItem('cart')) || [];

console.log(storedCart);

let totalPrice = 0; // Переменная для итоговой суммы

if (storedCart.length > 0) {
    let groupHtml = ''; // Строка для хранения HTML кода товаров

    // Цикл для обработки каждого товара в корзине
    storedCart.forEach(item => {
        groupHtml += `
            <div class="product">
                <div class="product-img" style="background-image: url('${item.dish.imageLinks}');"></div>
                <div class="product-details">
                    <h2>${item.dish.name}</h2>
                    <div class="modifiers-section">
                        ${item.modifiers.map(mod => `
                            <div class="product-description">
                                <label>${mod.name}:</label>
                                <span>${mod.quantity}</span>
                                ${mod.totalPrice > 0 ? `<span> - ${mod.totalPrice.toFixed(2)} MDL</span>` : ''}
                            </div>
                        `).join('')}
                    </div>
                    <div class="price">Цена: ${item.finalPrice} MDL</div>
                </div>
            </div>
        `;
        totalPrice += item.finalPrice; // Добавляем цену каждого товара в итоговую сумму
    });

    // Помещаем товары в блок с классом "product-list"
    const productList = document.querySelector('.product-list');
    productList.innerHTML = groupHtml;

    // Отображаем итоговую сумму
    const totalElement = document.querySelector('.total');
    totalElement.innerHTML = `<h3>Итоговая сумма: ${totalPrice} MDL</h3>`;
} else {
    document.querySelector('.total').innerHTML = `<h3>Корзина пуста</h3>`;
}

/*отображение доставки*/

window.onload = function() {
    // Изначально показываем apartment-fields и скрываем house-fields
    toggleFields();
}

function toggleFields() {
    const isHouseCheckbox = document.getElementById('isHouse');
    const apartmentFields = document.getElementById('apartment-fields');
    const houseFields = document.getElementById('house-fields');

    // Если галочка установлена - скрываем поля для квартиры, показываем дом
    if (isHouseCheckbox.checked) {
        apartmentFields.style.display = 'none';
    } else {
        apartmentFields.style.display = 'block';
    }
}

const checkoutButton = document.getElementById('submitOrder');

checkoutButton.addEventListener('click', () => {

    const formattedCart = storedCart.map(item => {
        // Базовый объект блюда
        const formattedItem = {
            productId: item.dish.id,
            price: item.finalPrice,
            type: "Product",  // Unique dish id
            amount: 1                 // Dish quantity
        };

        // Если есть модификаторы, добавляем их
        if (item.modifiers && item.modifiers.length > 0) {
            formattedItem.modifiers = item.modifiers.map(mod => {
                const modifier = {
                    productId: mod.id,
                    amount: mod.quantity
                };
                if (mod.isGroupModifier) {
                    modifier.productGroupId = mod.idGroup;
                }
                return modifier;
            });
        }


        return formattedItem;
    });

    // Собираем данные из формы
    const deliveryType = document.querySelector('input[name="delivery"]:checked').value;
    const street = document.getElementById("streetSelect").value;
    const house = document.getElementById("house").value;
    const customerName= document.getElementById("name").value;

    const entrance = document.getElementById("entrance").value;
    const apartment = document.getElementById("apartment").value;
    const floor = document.getElementById("floor").value;
    const intercom = document.getElementById("intercom").value;
    const isHouse = document.getElementById("isHouse").checked;

    // Получаем контактные данные
    const phone = document.getElementById("Phone").value;

    // Получаем способ оплаты
    const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked')?.value || "не выбран";

    let orderData = {}


    console.log(deliveryType);
    if(deliveryType == "delivery") {
        orderData = {
            id: null,
            phone: phone,
            orderTypeId: "76067ea3-356f-eb93-9d14-1fa00d082c4e",
            deliveryPoint: {
                address: {
                    street: {
                        id: street
                    },
                    house: house,
                    type: "legacy"
                },
            },
            customer: {
                name: customerName, // Здесь можно динамически вставить имя из формы, если есть
                type: "one-time"
            },
            items: formattedCart, // Добавляем корзину
            sourceKey: "Site Web",
            comment: "TEST",  // Пример комментария
            payments: [
                {
                    paymentTypeKind: "Cash",
                    sum: totalPrice,  // Пример суммы
                    isProcessedExternally: false,
                    paymentTypeId: paymentMethod === "card" ? "78d2e503-d026-4855-b067-11b10e8f2d9b" : "09322f46-578a-d210-add7-eec222a08871"  // Пример ID
                }
            ],
        };
    }

    else{
        orderData = {
            id: null,
            phone: phone,
            orderTypeId: "5b1508f9-fe5b-d6af-cb8d-043af587d5c2",
            customer: {
                name: customerName,  // Здесь можно динамически вставить имя из формы, если есть
                type: "one-time"
            },
            items: formattedCart, // Добавляем корзину
            sourceKey: "Site Web",
            comment: "TEST",  // Пример комментария
            payments: [
                {
                    paymentTypeKind: "Cash",
                    sum: totalPrice,  // Пример суммы
                    isProcessedExternally: false,
                    paymentTypeId: paymentMethod === "card" ? "78d2e503-d026-4855-b067-11b10e8f2d9b" : "09322f46-578a-d210-add7-eec222a08871"  // Пример ID
                }
            ],
        };
    }

    if (!isHouse && orderData.deliveryPoint && orderData.deliveryPoint.address) {
        orderData.deliveryPoint.address.entrance = entrance;
        orderData.deliveryPoint.address.flat = apartment;
        orderData.deliveryPoint.address.floor = floor;
        orderData.deliveryPoint.address.intercom = intercom;
    }

    //localStorage.clear();

    console.log(orderData)

    // fetch('/ordering', {
    //     method: 'POST',
    //     headers: {
    //         'Content-Type': 'application/json',
    //     },
    //     body: JSON.stringify(orderData)
    // })
    //     .then(response => {
    //         if (!response.ok) {
    //             throw new Error('Network response was not ok');
    //         }
    //         console.log('Order successfully submitted!');
    //         // Перенаправление на страницу заказа
    //         window.location.href = '/orderStatus';
    //     })
    //     .catch(error => {
    //         console.error('Error:', error);
    //     });

});

function toggleFields() {
    const isDelivery = document.querySelector('input[name="delivery"]:checked').value === 'delivery';
    document.getElementById('citySelect').required = isDelivery;
    document.getElementById('streetSelect').required = isDelivery;
    document.getElementById('house').required = isDelivery;

    const isHouseCheckbox = document.getElementById('isHouse').checked;
    document.getElementById('apartment-fields').style.display = isHouseCheckbox ? 'none' : 'block';

    if (!isHouseCheckbox && isDelivery) {
        document.getElementById('entrance').required = true;
        document.getElementById('floor').required = true;
        document.getElementById('apartment').required = true;
        document.getElementById('intercom').required = false;
    } else {
        document.getElementById('entrance').required = false;
        document.getElementById('floor').required = false;
        document.getElementById('apartment').required = false;
        document.getElementById('intercom').required = false;
    }
}

function validatePhone(phone) {
    const phoneRegex = /^\+373\d{8}$/;
    return phoneRegex.test(phone);
}

function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function showError(elementId) {
    document.getElementById(elementId).classList.add('error');
}

function hideError(elementId) {
    document.getElementById(elementId).classList.remove('error');
}

document.getElementById('submitOrder').addEventListener('click', function () {
    let valid = true;

    const isDelivery = document.querySelector('input[name="delivery"]:checked').value === 'delivery';
    const fieldsToCheck = [
        { id: 'citySelect' },
        { id: 'streetSelect' },
        { id: 'house' },
        { id: 'name' },
        { id: 'email', validator: validateEmail },
        { id: 'Phone', validator: validatePhone }
    ];

    if (isDelivery && !document.getElementById('isHouse').checked) {
        fieldsToCheck.push(
            { id: 'entrance' },
            { id: 'floor' },
            { id: 'apartment' },
            { id: 'intercom' }
        );
    }

    fieldsToCheck.forEach(field => {
        const element = document.getElementById(field.id);
        const isValid = field.validator ? field.validator(element.value) : element.value.trim() !== '';
        if (!isValid) {
            showError(field.id);
            valid = false;
        } else {
            hideError(field.id);
        }
    });

    if (valid) {
        // Процесс завершения заказа
    }
});

window.onload = function() {
    toggleFields();
};

