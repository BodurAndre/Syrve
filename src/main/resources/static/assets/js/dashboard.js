$(document).ready(function() {
    // Загрузка статистики дашборда
    loadDashboardStats();
    
    // Загрузка последних заказов
    loadRecentOrders();
    
    // Загрузка системного статуса
    loadSystemStatus();
    
    // Загрузка уведомлений
    loadNotifications();
    
    // Загрузка информации о пользователе
    loadUserInfo();
    
    // Обновление данных каждые 30 секунд
    setInterval(function() {
        loadDashboardStats();
        loadRecentOrders();
        loadSystemStatus();
        loadNotifications();
    }, 30000);
});

function loadDashboardStats() {
    $.ajax({
        url: '/admin/api/dashboard/stats',
        method: 'GET',
        success: function(data) {
            $('#totalOrders').text(data.totalOrders || 0);
            $('#pendingOrders').text(data.pendingOrders || 0);
            $('#totalProducts').text(data.totalProducts || 0);
            $('#totalRevenue').text('₽' + (data.totalRevenue || 0));
        },
        error: function() {
            console.log('Error loading dashboard stats');
        }
    });
}

function loadRecentOrders() {
    $.ajax({
        url: '/admin/api/orders/recent',
        method: 'GET',
        success: function(data) {
            const tableBody = $('#recentOrdersTable');
            tableBody.empty();
            
            if (data && data.length > 0) {
                data.forEach(order => {
                    const statusClass = getStatusClass(order.status);
                    const row = `
                        <tr>
                            <td>#${order.id}</td>
                            <td>${order.customerName || 'N/A'}</td>
                            <td>₽${order.totalPrice}</td>
                            <td><span class="badges ${statusClass}">${order.status}</span></td>
                            <td>${formatDate(order.createdAt)}</td>
                        </tr>
                    `;
                    tableBody.append(row);
                });
            } else {
                tableBody.append('<tr><td colspan="5" class="text-center">No recent orders</td></tr>');
            }
        },
        error: function() {
            console.log('Error loading recent orders');
        }
    });
}

function loadSystemStatus() {
    $.ajax({
        url: '/admin/api/system/status',
        method: 'GET',
        success: function(data) {
            // API Status
            const apiStatus = data.apiConnection ? 'bg-lightgreen' : 'bg-lightred';
            $('#apiStatus').removeClass().addClass('badges ' + apiStatus);
            $('#apiStatus').text(data.apiConnection ? 'Connected' : 'Disconnected');
            $('#apiLastUpdate').text(formatDateTime(data.apiLastUpdate));
            
            // Database Status
            const dbStatus = data.databaseConnection ? 'bg-lightgreen' : 'bg-lightred';
            $('#dbStatus').removeClass().addClass('badges ' + dbStatus);
            $('#dbStatus').text(data.databaseConnection ? 'Online' : 'Offline');
            $('#dbLastUpdate').text(formatDateTime(data.dbLastUpdate));
            
            // Product Sync Status
            const productSyncStatus = data.productSyncStatus === 'up_to_date' ? 'bg-lightgreen' : 'bg-lightyellow';
            $('#productSyncStatus').removeClass().addClass('badges ' + productSyncStatus);
            $('#productSyncStatus').text(data.productSyncStatus === 'up_to_date' ? 'Up to date' : 'Needs sync');
            $('#productSyncLastUpdate').text(formatDateTime(data.productSyncLastUpdate));
            
            // Address Sync Status
            const addressSyncStatus = data.addressSyncStatus === 'up_to_date' ? 'bg-lightgreen' : 'bg-lightyellow';
            $('#addressSyncStatus').removeClass().addClass('badges ' + addressSyncStatus);
            $('#addressSyncStatus').text(data.addressSyncStatus === 'up_to_date' ? 'Up to date' : 'Needs sync');
            $('#addressSyncLastUpdate').text(formatDateTime(data.addressSyncLastUpdate));
        },
        error: function() {
            console.log('Error loading system status');
        }
    });
}

function loadNotifications() {
    $.ajax({
        url: '/admin/api/notifications',
        method: 'GET',
        success: function(data) {
            const notificationList = $('#notificationList');
            const notificationCount = $('#notificationCount');
            
            notificationList.empty();
            notificationCount.text(data.length || 0);
            
            if (data && data.length > 0) {
                data.forEach(notification => {
                    const notificationItem = `
                        <li>
                            <a href="${notification.link || '#'}">
                                <div class="media">
                                    <span class="avatar avatar-sm flex-shrink-0">
                                        <img class="avatar-img rounded-circle" alt="User Image" src="assets/img/profiles/avatar-02.jpg">
                                    </span>
                                    <div class="media-body flex-grow-1">
                                        <p class="noti-details"><span class="noti-title">${notification.title}</span></p>
                                        <p class="noti-time"><span class="notification-time">${formatDateTime(notification.createdAt)}</span></p>
                                    </div>
                                </div>
                            </a>
                        </li>
                    `;
                    notificationList.append(notificationItem);
                });
            } else {
                notificationList.append('<li><div class="text-center">No notifications</div></li>');
            }
        },
        error: function() {
            console.log('Error loading notifications');
        }
    });
}

function loadUserInfo() {
    $.ajax({
        url: '/admin/getProfile',
        method: 'GET',
        success: function(data) {
            if (data) {
                const fullName = (data.firstName || '') + ' ' + (data.lastName || '');
                $('#userName').text(fullName.trim() || 'Admin User');
            }
        },
        error: function() {
            console.log('Error loading user info');
        }
    });
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

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU');
}

function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('ru-RU');
}

// Quick action handlers
$(document).on('click', '.quick-action-link', function(e) {
    e.preventDefault();
    const href = $(this).attr('href');
    
    // Показываем индикатор загрузки
    showLoader();
    
    // Переходим на страницу
    window.location.href = href;
});

function showLoader() {
    $('#global-loader').show();
}

function hideLoader() {
    $('#global-loader').hide();
}

// Обработчик очистки уведомлений
$(document).on('click', '.clear-noti', function(e) {
    e.preventDefault();
    
    $.ajax({
        url: '/admin/api/notifications/clear',
        method: 'POST',
        success: function() {
            loadNotifications();
        },
        error: function() {
            console.log('Error clearing notifications');
        }
    });
}); 