var order = []

document.addEventListener("DOMContentLoaded", () => {
    const orderId = window.location.pathname.split("/").pop(); // Получаем ID из URL
    const orderDetails = document.getElementById("orderDetails");

    fetch(`/api/order/${orderId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("Order not found");
            }
            return response.json();
        })
        .then(order => {
            this.order = order
            console.log(order)
            orderDetails.innerHTML = `
                        <font style="vertical-align: inherit;margin-bottom:25px;">
                            <font style="vertical-align: inherit;font-size:14px;color:#7367F0;font-weight:600;line-height: 35px;">&nbsp;</font>
                        </font><br>
                        <font style="vertical-align: inherit;">
                            <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">${order.id}</font>
                        </font><br>
                        <font style="vertical-align: inherit;">
                            <font style="vertical-align: inherit;font-size: 14px;color:${order.payment ? '#2E7D32' : '#FF2400'};font-weight: 400;">
                                ${order.payment ? 'Paid' : 'Not Paid'}
                            </font>
                        </font><br>
                        <font style="vertical-align: inherit;">
                            <font style="vertical-align: inherit;font-size: 14px;color:${order.status === 'Completed' ? '#2E7D32' : '#FF2400'};font-weight: 400;">
                                ${order.status}
                            </font>
                        </font><br>
                    `;
        })
        .catch(error => {
            console.error(error);
            alert("Не удалось загрузить данные заказа.");
        });
});