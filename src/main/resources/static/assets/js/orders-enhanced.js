$(document).ready(function() {
    // Инициализация DataTable
    initializeDataTable();
    
    // Загрузка заказов
    loadOrders();
    
    // Загрузка статистики
    loadOrdersStats();
    
    // Обработчики событий
    $('#refreshOrders').on('click', loadOrders);
    $('#exportOrders').on('click', exportOrders);
    $('#applyFilters').on('click', applyFilters);
    $('#clearFilters').on('click', clearFilters);
    $('#processOrder').on('click', processSelectedOrder);
    
    // Обработчик изменения статуса
    $(document).on('change', '.status-select', function() {
        const orderId = $(this).data('order-id');
        const newStatus = $(this).val();
        updateOrderStatus(orderId, newStatus);
    });
    
    // Обработчик просмотра деталей заказа
    $(document).on('click', '.view-order', function() {
        const orderId = $(this).data('order-id');
        viewOrderDetails(orderId);
    });
    
    // Обновление данных каждые 30 секунд
    setInterval(function() {
        loadOrdersStats();
    }, 30000);
});

let currentFilters = {};
let selectedOrderId = null;

function initializeDataTable() {
    $('#ordersTable').DataTable({
        "pageLength": 25,
        "order": [[5, "desc"]], // Sort by date descending
        "language": {
            "search": "Search:",
            "lengthMenu": "Show _MENU_ entries",
            "info": "Showing _START_ to _END_ of _TOTAL_ entries",
            "infoEmpty": "Showing 0 to 0 of 0 entries",
            "infoFiltered": "(filtered from _MAX_ total entries)",
            "emptyTable": "No orders available"
        },
        "columnDefs": [
            { "orderable": false, "targets": 6 } // Actions column
        ]
    });
}

function loadOrders() {
    showLoader();
    
    $.ajax({
        url: '/admin/api/orders',
        method: 'GET',
        data: currentFilters,
        success: function(data) {
            updateOrdersTable(data);
            hideLoader();
        },
        error: function(xhr, status, error) {
            console.error('Error loading orders:', error);
            showNotification('Error loading orders', false);
            hideLoader();
        }
    });
}

function loadOrdersStats() {
    $.ajax({
        url: '/admin/api/dashboard/stats',
        method: 'GET',
        success: function(data) {
            $('#totalOrders').text(data.totalOrders || 0);
            $('#pendingOrders').text(data.pendingOrders || 0);
            $('#totalRevenue').text('₽' + (data.totalRevenue || 0));
            
            // Вычисляем завершенные заказы
            const completed = (data.totalOrders || 0) - (data.pendingOrders || 0);
            $('#completedOrders').text(completed);
        },
        error: function() {
            console.log('Error loading orders stats');
        }
    });
}

function updateOrdersTable(orders) {
    const tableBody = $('#ordersTableBody');
    tableBody.empty();
    
    if (orders && orders.length > 0) {
        orders.forEach(order => {
            const statusClass = getStatusClass(order.status);
            const itemsCount = order.items ? order.items.length : 0;
            
            const row = `
                <tr>
                    <td>#${order.id}</td>
                    <td>${order.customerName || 'N/A'}</td>
                    <td>${itemsCount} items</td>
                    <td>₽${order.totalPrice || '0'}</td>
                    <td>
                        <select class="form-control status-select" data-order-id="${order.id}">
                            <option value="Pending" ${order.status === 'Pending' ? 'selected' : ''}>Pending</option>
                            <option value="Waiting" ${order.status === 'Waiting' ? 'selected' : ''}>Waiting</option>
                            <option value="Completed" ${order.status === 'Completed' ? 'selected' : ''}>Completed</option>
                            <option value="Error" ${order.status === 'Error' ? 'selected' : ''}>Error</option>
                        </select>
                    </td>
                    <td>${formatDateTime(order.createdAt)}</td>
                    <td>
                        <div class="actions">
                            <a href="javascript:void(0);" class="btn btn-sm btn-outline-primary view-order" data-order-id="${order.id}">
                                <i class="fas fa-eye"></i>
                            </a>
                            <a href="javascript:void(0);" class="btn btn-sm btn-outline-success" onclick="processOrder('${order.id}')">
                                <i class="fas fa-play"></i>
                            </a>
                            <a href="javascript:void(0);" class="btn btn-sm btn-outline-info" onclick="printOrder('${order.id}')">
                                <i class="fas fa-print"></i>
                            </a>
                        </div>
                    </td>
                </tr>
            `;
            tableBody.append(row);
        });
    } else {
        tableBody.append('<tr><td colspan="7" class="text-center">No orders found</td></tr>');
    }
    
    // Обновляем DataTable
    if ($.fn.DataTable.isDataTable('#ordersTable')) {
        $('#ordersTable').DataTable().destroy();
    }
    initializeDataTable();
}

function applyFilters() {
    currentFilters = {
        status: $('#statusFilter').val(),
        dateFrom: $('#dateFrom').val(),
        dateTo: $('#dateTo').val(),
        minAmount: $('#minAmount').val(),
        maxAmount: $('#maxAmount').val()
    };
    
    loadOrders();
}

function clearFilters() {
    $('#statusFilter').val('');
    $('#dateFrom').val('');
    $('#dateTo').val('');
    $('#minAmount').val('');
    $('#maxAmount').val('');
    
    currentFilters = {};
    loadOrders();
}

function updateOrderStatus(orderId, newStatus) {
    $.ajax({
        url: '/admin/editStatus',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(orderId),
        success: function(response) {
            showNotification('Order status updated successfully', true);
            loadOrdersStats();
        },
        error: function(xhr, status, error) {
            showNotification('Error updating order status: ' + (xhr.responseText || error), false);
            loadOrders(); // Reload to reset the select
        }
    });
}

function viewOrderDetails(orderId) {
    selectedOrderId = orderId;
    
    $.ajax({
        url: '/admin/api/order/' + orderId,
        method: 'GET',
        success: function(data) {
            displayOrderDetails(data);
            $('#orderDetailsModal').modal('show');
        },
        error: function(xhr, status, error) {
            showNotification('Error loading order details: ' + (xhr.responseText || error), false);
        }
    });
}

function displayOrderDetails(order) {
    const content = `
        <div class="row">
            <div class="col-md-6">
                <h6>Order Information</h6>
                <table class="table table-sm">
                    <tr><td><strong>Order ID:</strong></td><td>#${order.id}</td></tr>
                    <tr><td><strong>Status:</strong></td><td><span class="badges ${getStatusClass(order.status)}">${order.status}</span></td></tr>
                    <tr><td><strong>Total Amount:</strong></td><td>₽${order.totalPrice}</td></tr>
                    <tr><td><strong>Created:</strong></td><td>${formatDateTime(order.createdAt)}</td></tr>
                    <tr><td><strong>Comment:</strong></td><td>${order.comment || 'N/A'}</td></tr>
                </table>
            </div>
            <div class="col-md-6">
                <h6>Customer Information</h6>
                <table class="table table-sm">
                    <tr><td><strong>Name:</strong></td><td>${order.customerName || 'N/A'}</td></tr>
                    <tr><td><strong>Email:</strong></td><td>${order.customerEmail || 'N/A'}</td></tr>
                    <tr><td><strong>Phone:</strong></td><td>${order.customerPhone || 'N/A'}</td></tr>
                    <tr><td><strong>Address:</strong></td><td>${order.customerAddress || 'N/A'}</td></tr>
                </table>
            </div>
        </div>
        <div class="row mt-3">
            <div class="col-12">
                <h6>Order Items</h6>
                <div class="table-responsive">
                    <table class="table table-sm">
                        <thead>
                            <tr>
                                <th>Item</th>
                                <th>Quantity</th>
                                <th>Price</th>
                                <th>Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${generateOrderItemsHtml(order.items)}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        ${order.response ? `
        <div class="row mt-3">
            <div class="col-12">
                <h6>API Response</h6>
                <pre class="bg-light p-2">${JSON.stringify(order.response, null, 2)}</pre>
            </div>
        </div>
        ` : ''}
    `;
    
    $('#orderDetailsContent').html(content);
}

function generateOrderItemsHtml(items) {
    if (!items || items.length === 0) {
        return '<tr><td colspan="4" class="text-center">No items found</td></tr>';
    }
    
    return items.map(item => `
        <tr>
            <td>${item.name || 'Unknown Item'}</td>
            <td>${item.quantity || 1}</td>
            <td>₽${item.price || '0'}</td>
            <td>₽${item.totalPrice || '0'}</td>
        </tr>
    `).join('');
}

function processSelectedOrder() {
    if (selectedOrderId) {
        processOrder(selectedOrderId);
        $('#orderDetailsModal').modal('hide');
    }
}

function processOrder(orderId) {
    if (!confirm('Are you sure you want to process this order?')) {
        return;
    }
    
    showLoader();
    
    $.ajax({
        url: '/admin/api/orders/' + orderId + '/process',
        method: 'POST',
        success: function(response) {
            showNotification('Order processed successfully', true);
            loadOrders();
            loadOrdersStats();
        },
        error: function(xhr, status, error) {
            showNotification('Error processing order: ' + (xhr.responseText || error), false);
        },
        complete: function() {
            hideLoader();
        }
    });
}

function printOrder(orderId) {
    // Открываем новое окно для печати
    const printWindow = window.open('/admin/order/' + orderId + '/print', '_blank');
    if (printWindow) {
        printWindow.focus();
    }
}

function exportOrders() {
    const filters = Object.keys(currentFilters).length > 0 ? currentFilters : {};
    
    // Создаем временную форму для экспорта
    const form = $('<form>', {
        'method': 'POST',
        'action': '/admin/api/orders/export'
    });
    
    // Добавляем фильтры как скрытые поля
    Object.keys(filters).forEach(key => {
        if (filters[key]) {
            form.append($('<input>', {
                'type': 'hidden',
                'name': key,
                'value': filters[key]
            }));
        }
    });
    
    // Добавляем формат экспорта
    form.append($('<input>', {
        'type': 'hidden',
        'name': 'format',
        'value': 'excel'
    }));
    
    $('body').append(form);
    form.submit();
    form.remove();
}

function getStatusClass(status) {
    switch(status.toLowerCase()) {
        case 'completed':
            return 'bg-lightgreen';
        case 'pending':
            return 'bg-lightyellow';
        case 'error':
            return 'bg-lightred';
        case 'waiting':
            return 'bg-lightblue';
        default:
            return 'bg-lightgray';
    }
}

function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('ru-RU');
}

function showNotification(message, isSuccess) {
    // Используем существующую функцию уведомлений или создаем простую
    if (typeof showAlert === 'function') {
        showAlert(message, isSuccess ? 'success' : 'error');
    } else {
        alert(message);
    }
}

function showLoader() {
    $('#global-loader').show();
}

function hideLoader() {
    $('#global-loader').hide();
} 