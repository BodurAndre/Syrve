// Читаем данные из localStorage
const storedCart = localStorage.getItem('cart');

let totalPrice = 0;

if (storedCart) {
    const cart = JSON.parse(storedCart);  // Преобразуем строку обратно в массив
    console.log(cart);  // Теперь можно работать с данными

    let groupHtml = ''; // Строка для хранения HTML кода товаров
    let totalPrice = 0; // Переменная для хранения итоговой суммы

    // Цикл для обработки каждого товара в корзине
    cart.forEach(storedCart => {
        groupHtml += `
            <div class="product">
                <div class="product-img" style="background-image: url('${storedCart.dish.imageLinks}');"></div>
                <div class="product-details">
                    <h3>${storedCart.dish.name}</h3>
                    <div class="modifiers-section">
                        ${storedCart.modifiers.map(mod => `
                            <div class="product-description">
                                <label>${mod.name}:</label>
                                <span>${mod.quantity}</span>
                                <span class="price"> - ${mod.totalPrice.toFixed(2)} MDL</span>
                            </div>
                        `).join('')}
                    </div>
                    <!--
                        <div class="quantity-control">
                            <button class="quantity-btn" onclick="decreaseQuantity(${storedCart.dish.id})">-</button>
                            <input type="number" value="${storedCart.quantity}" min="1" id="quantity-${storedCart.dish.id}">
                            <button class="quantity-btn" onclick="increaseQuantity(${storedCart.dish.id})">+</button>
                        </div>
                    -->
                </div>
            </div>
        `;

        // Считаем итоговую цену для каждого товара
        console.log(storedCart.finalPrice)
        totalPrice += storedCart.finalPrice;
        console.log(totalPrice)
    });

    // Помещаем товары в блок с классом "product-list"
    const productList = document.querySelector('.product-list');
    productList.innerHTML = groupHtml;

    // Отображаем итоговую сумму
    const totalElement = document.querySelector('.total');
    totalElement.innerHTML = `<h3>Итоговая сумма: ${totalPrice.toFixed(2)} MDL</h3>`;
} else {
    console.log('Корзина пуста');
    document.querySelector('.total').innerHTML = `<h3>Корзина пуста</h3>`;
}

// Функции для увеличения и уменьшения количества товара
function increaseQuantity(dishId) {
    const inputField = document.getElementById(`quantity-${dishId}`);
    inputField.value = parseInt(inputField.value) + 1;
    updateCartQuantity(dishId, inputField.value);
    updateTotal();  // Обновляем итоговую сумму
}

function decreaseQuantity(dishId) {
    const inputField = document.getElementById(`quantity-${dishId}`);
    if (parseInt(inputField.value) > 1) {
        inputField.value = parseInt(inputField.value) - 1;
        updateCartQuantity(dishId, inputField.value);
        updateTotal();  // Обновляем итоговую сумму
    }
}

// Функция для обновления количества товара в localStorage
function updateCartQuantity(dishId, newQuantity) {
    const storedCart = JSON.parse(localStorage.getItem('cart'));
    const updatedCart = storedCart.map(item => {
        if (item.dish.id === dishId) {
            item.quantity = newQuantity;
        }
        return item;
    });
    localStorage.setItem('cart', JSON.stringify(updatedCart));
}

// Функция для пересчета итоговой суммы
function updateTotal() {
    const storedCart = JSON.parse(localStorage.getItem('cart'));
    let newTotal = 0;

    storedCart.forEach(item => {
        newTotal += item.quantity * item.dish.price;
    });

    const totalElement = document.querySelector('.total');
    totalElement.innerHTML = `<h3>Итоговая сумма: ${newTotal.toFixed(2)} MDL</h3>`;
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
        houseFields.style.display = 'block';
    } else {
        apartmentFields.style.display = 'block';
        houseFields.style.display = 'none';
    }
}
