document.addEventListener('DOMContentLoaded', function() {
    const cartItems = document.getElementById('modal-cart-items');
    const totalQuantity = document.getElementById('modal-total-quantity');
    const totalPrice = document.getElementById('modal-total-price');
    const cartCount = document.getElementById('cart-count');

    const cartIcon = document.getElementById('cart-icon');
    const cartModal = document.getElementById('cart-modal');
    const closeModal = document.getElementById('close-modal');

    let cart = [];

    function updateCart() {
        cartItems.innerHTML = '';
        let quantity = 0;
        let price = 0;

        cart.forEach(item => {
            const li = document.createElement('li');
            li.textContent = `${item.name} - $${item.price} x ${item.quantity}`;
            cartItems.appendChild(li);
            quantity += item.quantity;
            price += item.price * item.quantity;
        });

        totalQuantity.textContent = quantity;
        totalPrice.textContent = price.toFixed(2);
        cartCount.textContent = quantity; // Обновляем счетчик на иконке
    }

    function addToCart(name, price) {
        const existingItem = cart.find(item => item.name === name);
        if (existingItem) {
            existingItem.quantity += 1;
        } else {
            cart.push({ name, price: parseFloat(price), quantity: 1 });
        }
        updateCart();
    }

    document.querySelectorAll('.add-to-cart').forEach(button => {
        button.addEventListener('click', function() {
            const name = this.getAttribute('data-name');
            const price = this.getAttribute('data-price');
            addToCart(name, price);
        });
    });

    // Открытие модального окна
    document.addEventListener('DOMContentLoaded', function() {
        const cartIcon = document.getElementById('cartIcon');
        const cartModal = document.getElementById('cartModal');

        if (cartIcon && cartModal) {
            cartIcon.addEventListener('click', function() {
                cartModal.style.display = 'block';
            });
        }
    });

    document.addEventListener('DOMContentLoaded', function() {
        const closeModal = document.getElementById('closeModal');
        const cartModal = document.getElementById('cartModal');

        if (closeModal && cartModal) {
            closeModal.addEventListener('click', function() {
                cartModal.style.display = 'none';
            });
        }
    });


    // Закрытие модального окна при клике вне его содержимого
    window.addEventListener('click', function(event) {
        if (event.target == cartModal) {
            cartModal.style.display = 'none';
        }
    });
});
