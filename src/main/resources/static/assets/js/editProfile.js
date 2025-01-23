document.addEventListener('DOMContentLoaded', () => {
    const formInputs = document.querySelectorAll('.form-group input');
    const editButton = document.querySelector('.btn-edit');
    const submitButton = document.querySelector('.btn-save');
    let originalData = {};

    // Блокируем поля и сохраняем оригинальные данные
    formInputs.forEach(input => {
        input.disabled = true;
        originalData[input.name] = input.value;
    });

    // Разблокировка полей
    if (editButton) {
        editButton.addEventListener('click', () => {
            formInputs.forEach(input => {
                input.disabled = false;
            });
        });
    } else {
        showNotification('Edit button not found', false);
    }

    // Отправка данных
    if (submitButton) {
        submitButton.addEventListener('click', async () => {
            const updatedData = {};
            formInputs.forEach(input => {
                if (input.value !== originalData[input.name]) {
                    updatedData[input.name] = input.value;
                }
            });

            if (updatedData['oldPassword'] && updatedData['newPassword']) {
                const passwordCheck = await fetch('/admin/check-password', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ oldPassword: updatedData['oldPassword'] }),
                }).then(res => res.json());

                if (!passwordCheck.valid) {
                    showNotification('Incorrect old password!', false);
                    return;
                }
            }

            // Отправка данных на сервер для обновления
            await fetch('/admin/update-profile', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updatedData),
            }).then(res => {
                if (res.ok) {
                    showNotification('Data updated successfully!', true);
                    clearFieldsAndDisable(); // Очищаем поля и блокируем их снова
                } else {
                    showNotification('Failed to update data!', false);
                }
            });
        });
    } else {
        showNotification('Submit button not found', false);
    }
});

// Функция для очистки полей и их блокировки
function clearFieldsAndDisable() {
    const formInputs = document.querySelectorAll('.form-group input');
    formInputs.forEach(input => {
        input.value = '';    // Очищаем значение поля
        input.disabled = true; // Блокируем поле
    });
}

// Функция для отображения уведомлений
function showNotification(message, isSuccess) {
    const existingNotification = document.querySelector('.custom-notification');
    if (existingNotification) {
        existingNotification.remove();
    }

    const notification = document.createElement('div');
    notification.classList.add('custom-notification');
    notification.textContent = message;

    notification.style.position = 'fixed';
    notification.style.top = '20px';
    notification.style.right = '60px';
    notification.style.backgroundColor = isSuccess ? '#4CAF50' : '#f44336';
    notification.style.color = 'white';
    notification.style.padding = '10px 20px';
    notification.style.borderRadius = '5px';
    notification.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
    notification.style.fontSize = '16px';
    notification.style.zIndex = '9999';
    notification.style.opacity = '0';
    notification.style.transform = 'scale(0.9)';
    notification.style.transition = 'opacity 0.5s, transform 0.5s';

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.opacity = '1';
        notification.style.transform = 'scale(1)';
    }, 10);

    setTimeout(() => {
        notification.style.opacity = '0';
        notification.style.transform = 'scale(0.9)';
        setTimeout(() => {
            notification.remove();
        }, 500);
    }, 5000);
}
