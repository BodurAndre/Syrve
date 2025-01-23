$(document).ready(function () {
    fetch('/admin/getPhoto')
        .then((response) => {
            if (!response.ok) {
                throw new Error('Failed to fetch profile photo');
            }
            return response.json();
        })
        .then((data) => {
            const imgElements = document.getElementsByClassName('blah');
            for (let imgElement of imgElements) {
                imgElement.src = data.photoUrl;
            }
        })
        .catch((error) => {
            console.error('Error fetching profile photo:', error);
        });
});