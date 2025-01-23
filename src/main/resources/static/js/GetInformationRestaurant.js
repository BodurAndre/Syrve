document.addEventListener('DOMContentLoaded', () => {
    $.ajax({
        url: '/restaurant/getNameRestaurant',
        method: 'GET',
        success: function (data) {
            if (data) {
                const name = document.querySelectorAll('.restaurant-name');
                name.forEach(element => {
                    if (data.nameRestaurant != null)
                        element.textContent = data.nameRestaurant;
                    else
                        element.textContent = "Not name";
                });
                const phoneRestaurant = document.querySelectorAll('.phoneRestaurant')
                phoneRestaurant.forEach(element => {
                    if (data.phoneRestaurant != null)
                        element.textContent = data.phoneRestaurant;
                    else
                        element.textContent = "Not phone";
                });
                const emailRestaurant = document.querySelectorAll('.emailRestaurant');
                emailRestaurant.forEach(element => {
                    if (data.emailRestaurant != null)
                        element.textContent = data.emailRestaurant;
                    else
                        element.textContent = "Not email";
                });
                const addressRestaurant = document.querySelectorAll('.addressRestaurant');
                addressRestaurant.forEach(element => {
                    if (data.addressRestaurant != null)
                        element.textContent = data.addressRestaurant;
                    else
                        element.textContent = "Not address";
                });
            }
        },
        error: function () {
            showNotification('Ошибка при загрузке информации о ресторане.', false);
        }
    });
});
