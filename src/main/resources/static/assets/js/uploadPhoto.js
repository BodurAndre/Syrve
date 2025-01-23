// Показывать предварительный просмотр выбранного изображения
const imgInput = document.getElementById('imgInp');
const imgPreview = document.getElementById('blah');

imgInput.addEventListener('change', function () {
    const file = this.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            imgPreview.src = e.target.result; // Обновляем src изображения
        };
        reader.readAsDataURL(file);
    }
});

// Отправка фотографии на сервер
async function uploadPhoto() {
    const file = imgInput.files[0];
    if (!file) {
        alert('Please select an image to upload.');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/admin/profile/upload-photo', {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            const message = await response.text();
            alert('Photo uploaded successfully: ' + message);
        } else {
            alert('Failed to upload photo. Please try again.');
        }
    } catch (error) {
        console.error('Error uploading photo:', error);
        alert('An error occurred. Please try again.');
    }
}