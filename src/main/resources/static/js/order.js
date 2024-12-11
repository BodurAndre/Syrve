// Читаем данные из localStorage
const storedCart = JSON.parse(localStorage.getItem('cart')) || [];
console.log(storedCart);

let totalPrice = 0; // Variable for total price



if (storedCart.length > 0) {
    let groupHtml = ''; // String to hold the HTML of the products

    // Loop to process each item in the cart
    storedCart.forEach((item, index) => {
        groupHtml += `
            <div class="product">
                <div class="product-img" style="background-image: url('${item.dish.imageLinks}');"></div>
                <div class="product-details">
                    <h2>${item.dish.name}</h2>
                    <div class="modifiers-section">
                        ${item.modifiers.map(mod =>
            `<div class="product-description">
                                <label>${mod.name}:</label>
                                <span>${mod.quantity}</span>
                                ${mod.totalPrice > 0 ? `<span> - ${mod.totalPrice.toFixed(2)} MDL</span>` : ''}
                            </div>`
        ).join('')}
                    </div>
                    <div class="price">Цена: ${item.finalPrice} MDL</div>
                    <button class="remove-button" data-index="${index}">Удалить</button>
                </div>
            </div>
        `;
        totalPrice += item.finalPrice; // Add the price of each item to the total
    });

    // Insert the products into the "product-list" block
    const productList = document.querySelector('.product-list');
    productList.innerHTML = groupHtml;

    // Display the total price
    const totalElement = document.querySelector('.total');

    totalElement.innerHTML = `<h3>Итоговая сумма: ${totalPrice} MDL</h3>`;

    // Add event listener to remove buttons
    const removeButtons = document.querySelectorAll('.remove-button');
    removeButtons.forEach(button => {
        button.addEventListener('click', function() {
            const index = this.getAttribute('data-index');
            removeItemFromCart(index);
        });
    });
} else {
    document.querySelector('.total').innerHTML = `<h3>Корзина пуста</h3>`;
}

// Function to remove item from cart and update localStorage
function removeItemFromCart(index) {
    // Remove the item from the cart array
    storedCart.splice(index, 1);
    // Update the cart in localStorage
    localStorage.setItem('cart', JSON.stringify(storedCart));

    // Re-render the cart
    location.reload();
}

/* Display Delivery Option */
window.onload = function() {
    // Initially show apartment fields and hide house fields
    toggleFields();
}

function toggleFields() {
    const isHouseCheckbox = document.getElementById('isHouse');
    const apartmentFields = document.getElementById('apartment-fields');
    const houseFields = document.getElementById('house-fields');

    // If checkbox is checked, hide apartment fields and show house fields
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
            price: item.dish.price,
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

    const dishes= storedCart.map(item => {
        // Базовый объект блюда
        const formattedItem = {
            imageLinks: item.dish.imageLinks,
            price: item.dish.price, // Unique dish id
            amount: item.dish.amount,
            discount: 0,
            subtotal: item.finalPrice,
            name: item.dish.name
        };

        // Если есть модификаторы, добавляем их
        if (item.modifiers && item.modifiers.length > 0) {
            formattedItem.modifiers = item.modifiers.map(mod => {
                const modifier = {
                    name: mod.name,
                    amount: mod.quantity,
                    subtotal: mod.totalPrice,
                    price: mod.price
                };
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
    const doorphone = document.getElementById("intercom").value;
    const isHouse = document.getElementById("isHouse").checked;
    const email = document.getElementById("email").value;

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

    if(deliveryType == "pickup"){
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
        orderData.deliveryPoint.address.doorphone = doorphone;
    }


    //localStorage.clear();


    let order = {
        email: email, // email для передачи в query
        dishes: dishes,
        json: orderData, // содержимое заказа
        totalPrice: totalPrice
    };

    console.log(orderData);
    console.log(order);

    fetch(`/saveOrder`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
        },
        body: JSON.stringify(order), // отправляем весь объект
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || 'Unknown error');
                });
            }
            return response.json();
        })
        .then(data => {
            console.log('Order processed successfully', data);
            showNotification('Order processed successfully', true);
        })
        .catch(error => {
            console.error('Error:', error.message);
            showNotification(error.message, false);
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
    notification.style.top = '20px';
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

