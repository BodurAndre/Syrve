let cart = [];

document.addEventListener("DOMContentLoaded", function () {
    const savedCart = localStorage.getItem('cart');
    if (savedCart) {
        cart = JSON.parse(savedCart);
        cartCount.textContent = cart.length;
    }
    console.log(cart)

    Promise.all([
        fetch('/menu/dishes').then(response => response.json()),
        fetch('/menu/products').then(response => response.json())
    ])
        .then(([dishes, products]) => {
            displayDishes(dishes);
            displayProducts(products);
        })
        .catch(error => console.error('Error:', error));
});

const modal = document.getElementById("modal");
const cartModal = document.getElementById("cartModal");
const closeButtons = document.querySelectorAll(".close-button"); // Select all close buttons
const modalBody = document.querySelector(".modal-body");
const cartButton = document.getElementById("cartButton");
const cartCount = document.getElementById("cartCount");
const cartItems = document.getElementById("cartItems");
const totalPriceElement = document.getElementById("totalPrice");

// Attach event listeners to all close buttons to close modals
closeButtons.forEach(button => {
    button.addEventListener('click', closeModal);
});

// Function to close modals
function closeModal() {
    modal.style.display = "none";
    cartModal.style.display = "none";
}

// Function to open the dish modal
function openModal(dish) {
    if (!dish) {
        console.error("Блюдо не найдено.");
        return;
    }

    // Безопасные вызовы map
    const modifierGroupsHtml = Array.isArray(dish.modifierGroups)
        ? dish.modifierGroups.map(group => `
            <h4>${group.name} Бесплатно: ${group.freeOfChargeAmount}</h4>
            ${group.modifiers.map(modifier => `
                <div class="product-description">
                    <label>${modifier.name}:</label>
                    <input type="number" 
                           value="${modifier.defaultQuantity}" 
                           min="${modifier.minQuantity}" 
                           max="${group.maxQuantity}" 
                           data-modifier-id="${modifier.id}" 
                           data-group-id="${group.name}"
                           class="modifier-input">
                    <span>Цена: ${modifier.currentPrice} MDL</span>
                </div>
            `).join('')}
        `).join('')
        : '';

    const individualModifiersHtml = Array.isArray(dish.modifiers)
        ? dish.modifiers.map(modifier => `
            <div class="product-description">
                <label>${modifier.name}:</label>
                <input type="number" 
                       value="${modifier.defaultQuantity}" 
                       min="${modifier.minQuantity}" 
                       max="${modifier.maxQuantity}" 
                       data-modifier-id="${modifier.id}" 
                       class="modifier-input">
                <span>Цена: ${modifier.currentPrice} MDL | Бесплатно: ${modifier.freeOfChargeAmount}</span>
            </div>
        `).join('')
        : '';

    modalBody.innerHTML = `
        <div class="modal-text">
            <h3 class="product-title">${dish.name}</h3>
            <img src="${dish.imageLinks}" alt="${dish.name}" class="product-image">
            <div class="modifiers-section">
                ${modifierGroupsHtml}
                <br>
                ${individualModifiersHtml}
            </div>
            <div class="product-footer">
                <span id="finalPrice">${dish.price} MDL</span>
                <button id="addToCartButton" class="add-to-cart">Добавить в корзину</button>
            </div>
        </div>
    `;

    modal.style.display = "block";

    const inputs = document.querySelectorAll('.modifier-input');
    const finalPriceElement = document.getElementById('finalPrice');
    let finalPrice = 0;

    // Update price when modifier quantity changes
    inputs.forEach(input => {
        input.addEventListener('input', (event) => {

            let group = dish.modifierGroups.find(g => g.name === input.dataset['groupId']);
            let totalQuantity = 0

            if (group) {
                // Handle group modifiers
                inputs.forEach(i => {
                    if (group.modifiers.some(m => m.id == i.dataset['modifierId'])) {
                        totalQuantity += parseInt(i.value) || 0; // Add NaN check
                    }
                });

                // If the total quantity exceeds the maximum, adjust the value
                if (totalQuantity > group.maxQuantity) {
                    const excess = totalQuantity - group.maxQuantity;
                    const newValue = parseInt(event.target.value) - excess;

                    if (newValue >= 0) {
                        event.target.value = newValue; // Update value if not negative
                    } else {
                        event.target.value = 0; // Set value to 0 if it becomes negative
                    }
                }
            } else {
                // Handle regular (non-grouped) modifiers
                let modifier = dish.modifiers.find(m => m.id === input.dataset['modifierId']);
                if (modifier) {
                    totalQuantity = parseInt(input.value) || 0; // Update totalQuantity for regular modifiers

                    // If the total quantity exceeds the max, adjust the value
                    if (totalQuantity > modifier.maxQuantity) {
                        const excess = totalQuantity - modifier.maxQuantity;
                        const newValue = parseInt(event.target.value) - excess;

                        if (newValue >= 0) {
                            event.target.value = newValue;
                        } else {
                            event.target.value = 0;
                        }
                    }
                }
            }

            updateFinalPrice();
        });
    });


    function updateFinalPrice() {
        let priceDish = parseFloat(dish.price);
        const inputArray = Array.from(inputs);

        // Хранение оставшегося бесплатного количества для каждой группы
        const groupFreeQuantities = {};

        // Инициализация оставшегося бесплатного количества для групп
        dish.modifierGroups.forEach(group => {
            groupFreeQuantities[group.name] = group.freeOfChargeAmount || 0;
        });

        inputArray.forEach(input => {
            const modifier = dish.modifiers.concat(...dish.modifierGroups.map(g => g.modifiers))
                .find(m => m.id == input.dataset['modifierId']);
            if (modifier) {
                const quantity = parseInt(input.value) || 0;
                let chargeableQuantity = 0;

                if (modifier.groupModifier) {
                    const group = dish.modifierGroups.find(g => g.modifiers.some(m => m.id === modifier.id));
                    if (group) {
                        const groupFreeLeft = groupFreeQuantities[group.name] || 0;

                        const freeForThisModifier = Math.min(quantity, groupFreeLeft);
                        groupFreeQuantities[group.name] -= freeForThisModifier;

                        chargeableQuantity = Math.max(0, quantity - freeForThisModifier);
                    }
                } else {
                    const freeForModifier = modifier.freeOfChargeAmount || 0;
                    chargeableQuantity = Math.max(0, quantity - freeForModifier);
                }

                priceDish += chargeableQuantity * (modifier.currentPrice || 0);
            }
        });


        // Обновляем итоговую цену
        finalPriceElement.textContent = `${priceDish} MDL`;
        finalPrice = priceDish;
        console.log(finalPrice)
    }

    const addToCartButton = document.getElementById('addToCartButton');

    if (!addToCartButton) {
        console.error('Элемент с id "addToCartButton" не найден');
        return;
    }

    addToCartButton.addEventListener('click', () => {
        const selectedModifiers = [];
        inputs.forEach(input => {
            if (parseInt(input.value) > 0) {
                const quantity = parseInt(input.value);
                const modifier = dish.modifiers.concat(...dish.modifierGroups.map(g => g.modifiers))
                    .find(m => m.id == input.dataset['modifierId']);


                if (modifier) {
                    const baseModifier = {
                        id: modifier.id,
                        name: modifier.name,
                        quantity: quantity,
                        amount: 1,
                        isGroupModifier: modifier.groupModifier,
                        idGroup: modifier.idGroup,
                        price: modifier.currentPrice,
                        totalPrice: Math.abs((quantity - modifier.freeOfChargeAmount) * modifier.currentPrice),
                    };
                    console.log(baseModifier)
                    selectedModifiers.push(baseModifier);
                }
            }
        });

        console.log(finalPrice)
        if(finalPrice == 0){
            cart.push({
                dish: dish,
                modifiers: selectedModifiers,
                finalPrice: parseFloat(dish.price)
            });
        }
        else{
            cart.push({
                dish: dish,
                modifiers: selectedModifiers,
                finalPrice: finalPrice
            });
        }

        // Сохранение корзины в LocalStorage
        localStorage.setItem('cart', JSON.stringify(cart));

        if (cartCount) {
            cartCount.textContent = cart.length;
        }
        closeModal();
    });

}


function displayDishes(dishes) {
    const groups = {};

    console.log(dishes);

    // Separate dishes by groups
    dishes.forEach(dish => {
        if (dish.isIncludedMenu) {
            if (!groups[dish.groupName]) {
                groups[dish.groupName] = [];
            }
            groups[dish.groupName].push(dish);
        }
    });

    // Create HTML for each group
    for (const [groupName, groupDishes] of Object.entries(groups)) {
        const groupElement = document.createElement('div');
        groupElement.classList.add('col-md-6', 'col-lg-4');

        let groupHtml = `
            <div class="menu-wrap">
                <div class="heading-menu text-center ftco-animate fadeInUp ftco-animated">
                    <h3>${groupName}</h3>
                </div>
        `;

        groupDishes.forEach(dish => {
            groupHtml += `
                <div class="menus border-bottom-0 d-flex ftco-animate fadeInUp ftco-animated">
                    <div 
                        class="menu-img img" 
                        style="background-image: url(${dish.imageLinks});" 
                        data-dish-id="${dish.id}"
                        title="${dish.name}" 
                        style="cursor: pointer;"
                    ></div>
                    <div class="text">
                        <div class="d-flex">
                            <div class="one-half view-modal" data-dish-id="${dish.id}" title="${dish.name}" style="cursor: pointer;">
                                <h3>${dish.name}</h3>
                            </div>
                            <div class="one-forth">
                                <span class="price">${dish.price} MDL</span>
                            </div>
                        </div>
                        <p><span>${dish.weight} gr</span></p>
                    </div>
                </div>
                <span class="flat flaticon-bread" style="left: 0;"></span>
                <span class="flat flaticon-breakfast" style="right: 0;"></span>
            `;
        });

        groupHtml += '</div>';
        groupElement.innerHTML = groupHtml;

        dishesContainer.appendChild(groupElement);

        // Attach click event listeners to both image and name divs
        const clickableElements = groupElement.querySelectorAll('.menu-img.img, .view-modal');

        clickableElements.forEach(clickableElement => {
            clickableElement.addEventListener('click', () => {
                const dishId = clickableElement.dataset.dishId;
                const dish = groupDishes.find(d => d.id === dishId);
                if (dish) {
                    openModal(dish);
                } else {
                    console.error(`Dish with ID ${dishId} not found.`);
                }
            });
        });
    }

}


function displayProducts(dishes) {
    const groups = {};

    // Separate dishes by groups
    dishes.forEach(dish => {
        if (dish.isIncludedMenu) {
            if (!groups[dish.groupName]) {
                groups[dish.groupName] = [];
            }
            groups[dish.groupName].push(dish);
        }
    });

    // Create HTML for each group
    for (const [groupName, groupDishes] of Object.entries(groups)) {
        const groupElement = document.createElement('div');
        groupElement.classList.add('col-md-6', 'col-lg-4');

        let groupHtml = `
            <div class="menu-wrap">
                <div class="heading-menu text-center ftco-animate fadeInUp ftco-animated">
                    <h3>${groupName}</h3>
                </div>
        `;

        groupDishes.forEach(dish => {
            groupHtml += `
                <div class="menus border-bottom-0 d-flex ftco-animate fadeInUp ftco-animated">
                    <div 
                        class="menu-img img" 
                        style="background-image: url(${dish.imageLinks});" 
                        data-dish-id="${dish.id}"
                        title="${dish.name}" 
                        style="cursor: pointer;"
                    ></div>
                    <div class="text">
                        <div class="d-flex">
                            <div class="one-half view-modal" data-dish-id="${dish.id}" title="${dish.name}" style="cursor: pointer;">
                                <h3>${dish.name}</h3>
                            </div>
                            <div class="one-forth">
                                <span class="price">${dish.price} MDL</span>
                            </div>
                        </div>
                    </div>
                </div>
                <span class="flat flaticon-bread" style="left: 0;"></span>
                <span class="flat flaticon-breakfast" style="right: 0;"></span>
            `;
        });

        groupHtml += '</div>';
        groupElement.innerHTML = groupHtml;

        dishesContainer.appendChild(groupElement);

        // Attach click event listeners to both image and name divs
        const clickableElements = groupElement.querySelectorAll('.menu-img.img, .view-modal');

        clickableElements.forEach(clickableElement => {
            clickableElement.addEventListener('click', () => {
                const dishId = clickableElement.dataset.dishId;
                const dish = groupDishes.find(d => d.id === dishId);
                if (dish) {
                    openModal(dish);
                } else {
                    console.error(`Dish with ID ${dishId} not found.`);
                }
            });
        });
    }
}



cartButton.addEventListener('click', () => {
    window.location.href = "/order";
});

// Display cart content
function displayCart() {
    if (!cartItems || !cartCount || !totalPriceElement) {
        console.error('Не найдены необходимые элементы для отображения корзины.');
        return;
    }
    console.log('Найдены необходимые элементы для отображения корзины.');
    cartItems.innerHTML = '';
    let totalPrice = 0;

    if (cart.length === 0) {
        cartItems.innerHTML = '<p>Ваша корзина пуста.</p>';
        totalPriceElement.textContent = 'Общая сумма: 0 MDL';
        return;
    }

    cart.forEach((item, index) => {
        const itemElement = document.createElement('li');
        itemElement.innerHTML = `
        <div class="modal-text">
            <h3 class="product-title">${item.dish.name}</h3>
            <img src="${item.dish.imageLinks}" alt="${item.dish.name}" class="product-image">
            <div class="modifiers-section">
                ${item.modifiers.map(mod => `
                    <div class="product-description">
                        <label>${mod.name}:</label>
                        <span>${mod.quantity}</span>
                        ${mod.totalPrice > 0 ? `<span>Цена: ${mod.totalPrice} MDL</span>` : ''}
                    </div>
                `).join('')}
            </div>
            <div class="product-footer">
                <strong>Итоговая цена: ${item.finalPrice} MDL</strong>
                <button class="remove-button" data-index="${index}">Удалить</button>
            </div>
        </div>  
    `;
        cartItems.appendChild(itemElement);
        totalPrice += item.finalPrice;
    });


    totalPriceElement.textContent = `Общая сумма: ${totalPrice} MDL`;

    // Add event listeners for "Remove" buttons
    const inputs = document.querySelectorAll('.modifier-input-cart');
    inputs.forEach(input => {
        input.addEventListener('input', (event) => {
            const itemIndex = event.target.closest('.product-footer').querySelector('.remove-button').dataset.index;
            const cartItem = cart[itemIndex];
            const modifier = cartItem.modifiers.find(m => m.id == input.dataset.modifierId);
            if (modifier) {
                let newQuantity = parseInt(input.value);
                if (newQuantity < modifier.minQuantity) newQuantity = modifier.minQuantity;
                if (newQuantity > modifier.maxQuantity) newQuantity = modifier.maxQuantity;

                modifier.quantity = newQuantity;
                modifier.totalPrice = (newQuantity - modifier.freeOfChargeAmount) * modifier.price;
                input.nextElementSibling.textContent = `Price: ${modifier.quantity} x ${modifier.price} MDL = ${modifier.totalPrice} MDL`;

                updateFinalPriceInCart(cartItem);
            }
        });
    });

    // Add event listeners for "Remove" buttons
    const removeButtons = document.querySelectorAll('.remove-button');
    removeButtons.forEach(button => {
        button.addEventListener('click', (event) => {
            const index = event.target.dataset.index;
            cart.splice(index, 1);  // Удаляем элемент из массива cart
            localStorage.setItem('cart', JSON.stringify(cart));  // Обновляем localStorage
            cartCount.textContent = cart.length;  // Обновляем количество элементов в корзине
            displayCart();  // Перерисовываем корзину
        });
    });

}


// Update final price in cart
function updateFinalPriceInCart(cartItem) {
    let finalPrice = parseFloat(cartItem.dish.price);
    cartItem.modifiers.forEach(modifier => {
        if (modifier.quantity > modifier.freeOfChargeAmount) {
            finalPrice += (modifier.quantity - modifier.freeOfChargeAmount) * modifier.price;
        }
    });
    cartItem.finalPrice = finalPrice;
    displayCart();
}

// Close modal on outside click
window.addEventListener("click", (event) => {
    if (event.target === cartModal || event.target === modal) {
        closeModal();
    }
});