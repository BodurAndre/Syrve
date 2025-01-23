document.addEventListener('DOMContentLoaded', async () => {
    try {
        // Получаем данные профиля через GET-запрос
        const response = await fetch('/admin/getProfile');
        if (!response.ok) {
            throw new Error('Failed to fetch profile data');
        }
        const userData = await response.json();

        // Заполняем placeholder'ы значениями из ответа
        document.querySelector('input[name="firstName"]').placeholder = userData.firstName || '';
        document.querySelector('input[name="lastName"]').placeholder = userData.lastName || '';
        document.querySelector('input[name="email"]').placeholder = userData.email || '';
        document.querySelector('input[name="phone"]').placeholder = userData.phone || '+000 000 0000';
        document.querySelector('input[name="oldPassword"]').placeholder = 'Enter old password';
        document.querySelector('input[name="newPassword"]').placeholder = 'Enter new password';

        // Заполняем имя и фамилию в профиле
        const profileNameElements = document.querySelectorAll('.name');  // Используем querySelectorAll для выбора всех элементов с классом

        profileNameElements.forEach(profileNameElement => {
            if (userData.firstName && userData.lastName) {
                profileNameElement.textContent = `${userData.firstName} ${userData.lastName}`;
            } else if (userData.firstName) {
                profileNameElement.textContent = userData.firstName;
            } else {
                profileNameElement.textContent = '';  // Пустая строка, если данных нет
            }
        });

    } catch (error) {
        console.error('Error fetching profile data:', error);
    }
});
