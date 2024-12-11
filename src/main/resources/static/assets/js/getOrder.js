document.addEventListener("DOMContentLoaded", () => {
    const orderId = window.location.pathname.split("/").pop();
    const orderDetails = document.getElementById("orderDetails");
    const customerInfo = document.getElementById("customerInfo");
    const addressInfo = document.getElementById("addressInfo");
    const dishesTable = document.getElementById("dishesTable");

    fetch(`/api/order/${orderId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("Order not found");
            }
            return response.json();
        })
        .then(order => {
            // Fill in the order details
            orderDetails.innerHTML = `
                <font style="vertical-align: inherit;">
                    <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">ID: ${order.id}</font>
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

            // Fill in customer information
            customerInfo.innerHTML = `
                <font style="vertical-align: inherit;">
                    <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">Name: ${order.customer?.name || 'N/A'}</font>
                </font><br>
                <font style="vertical-align: inherit;">
                    <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">Email: ${order.customer?.email || 'N/A'}</font>
                </font><br>
                <font style="vertical-align: inherit;">
                    <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">Phone: ${order.customer?.phone || 'N/A'}</font>
                </font><br>
            `;

            // Fill in address information
            addressInfo.innerHTML = order.address
                ? `
                    <font style="vertical-align: inherit;">
                        <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">City: ${order.address.city}</font>
                    </font><br>
                    <font style="vertical-align: inherit;">
                        <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">Street: ${order.address.street}</font>
                    </font><br>
                    <font style="vertical-align: inherit;">
                        <font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">House: ${order.address.house}</font>
                    </font><br>
                `
                : `<font style="vertical-align: inherit;font-size: 14px;color:#000;font-weight: 400;">Pickup</font><br>`;

            // Fill in the dishes
            order.dishes.forEach(dish => {
                const row = document.createElement("tr");
                row.classList.add("details");
                row.style.borderBottom = "1px solid #E9ECEF";

                row.innerHTML = `
                    <td style="padding: 10px;vertical-align: top; display: flex;align-items: center;">
                        <img src="${dish.imageLinks}" alt="${dish.name}" class="me-2" style="width:40px;height:40px;">
                        ${dish.name}
                    </td>
                    <td style="padding: 10px;vertical-align: top;">
                        ${dish.amount}
                    </td>
                    <td style="padding: 10px;vertical-align: top;">
                        ${dish.price}
                    </td>
                    <td style="padding: 10px;vertical-align: top;">
                        ${dish.discount}
                    </td>
                    <td style="padding: 10px;vertical-align: top;">
                        ${dish.subtotal}
                    </td>
                `;
                dishesTable.appendChild(row);
            });
        })
        .catch(error => {
            console.error(error);
            alert("Не удалось загрузить данные заказа.");
        });
});