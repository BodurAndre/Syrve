$(document).ready(function() {
    // Загрузка статистики продуктов
    loadProductStats();
    
    // Загрузка истории синхронизации
    loadSyncHistory();
    
    // Проверка статуса API
    checkApiStatus();
    
    // Обработчики событий
    $('#startSync').on('click', startProductSync);
    $('#checkStatus').on('click', checkApiStatus);
    $('#clearProducts').on('click', clearAllProducts);
    
    // Обновление данных каждые 30 секунд
    setInterval(function() {
        loadProductStats();
        checkApiStatus();
    }, 30000);
});

function loadProductStats() {
    $.ajax({
        url: '/admin/api/dashboard/stats',
        method: 'GET',
        success: function(data) {
            $('#totalProducts').text(data.totalProducts || 0);
            
            // Загружаем детальную статистику продуктов
            loadDetailedProductStats();
        },
        error: function() {
            console.log('Error loading product stats');
        }
    });
}

function loadDetailedProductStats() {
    $.ajax({
        url: '/admin/api/products/stats',
        method: 'GET',
        success: function(data) {
            $('#totalDishes').text(data.totalDishes || 0);
            $('#totalModifiers').text(data.totalModifiers || 0);
            $('#lastSync').text(data.lastSync || 'Never');
        },
        error: function() {
            console.log('Error loading detailed product stats');
        }
    });
}

function checkApiStatus() {
    $('#apiStatusText').text('Checking...');
    $('#apiStatusDot').removeClass().addClass('status-dot checking');
    
    $.ajax({
        url: '/admin/api/system/status',
        method: 'GET',
        success: function(data) {
            if (data.apiConnection) {
                $('#apiStatusText').text('Connected');
                $('#apiStatusDot').removeClass().addClass('status-dot connected');
            } else {
                $('#apiStatusText').text('Disconnected');
                $('#apiStatusDot').removeClass().addClass('status-dot disconnected');
            }
            
            // Обновляем отображение API login
            updateApiLoginDisplay();
        },
        error: function() {
            $('#apiStatusText').text('Error');
            $('#apiStatusDot').removeClass().addClass('status-dot error');
        }
    });
}

function updateApiLoginDisplay() {
    $.ajax({
        url: '/admin/viewCompany',
        method: 'GET',
        success: function(data) {
            if (data && data.apiLogin) {
                $('#apiLoginDisplay').text(data.apiLogin);
            } else {
                $('#apiLoginDisplay').text('Not configured');
            }
        },
        error: function() {
            $('#apiLoginDisplay').text('Error loading');
        }
    });
}

function startProductSync() {
    if (!confirm('Are you sure you want to start product synchronization? This may take several minutes.')) {
        return;
    }
    
    // Показываем прогресс
    $('#syncProgress').show();
    $('#noSync').hide();
    $('#startSync').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Syncing...');
    
    // Сбрасываем прогресс
    resetProgress();
    
    // Добавляем лог
    addSyncLog('Starting product synchronization...', 'info');
    
    // Начинаем синхронизацию
    $.ajax({
        url: '/admin/saveProducts',
        method: 'GET',
        success: function(response) {
            updateProgress(100, 'Sync completed successfully');
            addSyncLog('Product synchronization completed successfully!', 'success');
            $('#startSync').prop('disabled', false).html('<i class="fas fa-sync-alt"></i> Start Sync');
            
            // Обновляем статистику
            loadProductStats();
            loadSyncHistory();
        },
        error: function(xhr, status, error) {
            updateProgress(0, 'Sync failed');
            addSyncLog('Error during synchronization: ' + (xhr.responseText || error), 'error');
            $('#startSync').prop('disabled', false).html('<i class="fas fa-sync-alt"></i> Start Sync');
        }
    });
    
    // Симуляция прогресса (в реальном приложении это должно быть через WebSocket)
    simulateProgress();
}

function simulateProgress() {
    let progress = 0;
    const interval = setInterval(function() {
        progress += Math.random() * 20;
        if (progress >= 90) {
            progress = 90;
            clearInterval(interval);
        }
        updateProgress(progress);
    }, 1000);
}

function updateProgress(percent, step = '') {
    $('#progressBar').css('width', percent + '%').text(Math.round(percent) + '%');
    
    // Обновляем шаги
    if (percent >= 25) {
        $('#step1Icon').html('<i class="fas fa-check text-success"></i>');
    }
    if (percent >= 50) {
        $('#step2Icon').html('<i class="fas fa-check text-success"></i>');
    }
    if (percent >= 75) {
        $('#step3Icon').html('<i class="fas fa-check text-success"></i>');
    }
    if (percent >= 100) {
        $('#step4Icon').html('<i class="fas fa-check text-success"></i>');
    }
    
    if (step) {
        addSyncLog(step, 'info');
    }
}

function resetProgress() {
    $('#progressBar').css('width', '0%').text('0%');
    $('#step1Icon').html('<i class="fas fa-circle"></i>');
    $('#step2Icon').html('<i class="fas fa-circle"></i>');
    $('#step3Icon').html('<i class="fas fa-circle"></i>');
    $('#step4Icon').html('<i class="fas fa-circle"></i>');
    $('#syncLog').empty();
}

function addSyncLog(message, type = 'info') {
    const timestamp = new Date().toLocaleTimeString();
    const logClass = type === 'error' ? 'text-danger' : type === 'success' ? 'text-success' : 'text-info';
    
    const logEntry = `
        <div class="log-entry ${logClass}">
            <small>[${timestamp}]</small> ${message}
        </div>
    `;
    
    $('#syncLog').append(logEntry);
    $('#syncLog').scrollTop($('#syncLog')[0].scrollHeight);
}

function clearAllProducts() {
    if (!confirm('Are you sure you want to clear ALL products? This action cannot be undone!')) {
        return;
    }
    
    if (!confirm('This will delete all products, dishes, and modifiers. Are you absolutely sure?')) {
        return;
    }
    
    $('#clearProducts').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Clearing...');
    
    $.ajax({
        url: '/admin/api/products/clear',
        method: 'POST',
        success: function(response) {
            showNotification('All products cleared successfully', true);
            loadProductStats();
            $('#clearProducts').prop('disabled', false).html('<i class="fas fa-trash"></i> Clear All Products');
        },
        error: function(xhr, status, error) {
            showNotification('Error clearing products: ' + (xhr.responseText || error), false);
            $('#clearProducts').prop('disabled', false).html('<i class="fas fa-trash"></i> Clear All Products');
        }
    });
}

function loadSyncHistory() {
    $.ajax({
        url: '/admin/api/products/sync-history',
        method: 'GET',
        success: function(data) {
            updateSyncHistoryTable(data);
        },
        error: function() {
            console.log('Error loading sync history');
        }
    });
}

function updateSyncHistoryTable(history) {
    const tableBody = $('#syncHistoryTable');
    tableBody.empty();
    
    if (history && history.length > 0) {
        history.forEach(item => {
            const statusClass = item.status === 'success' ? 'bg-lightgreen' : 
                              item.status === 'error' ? 'bg-lightred' : 'bg-lightyellow';
            
            const row = `
                <tr>
                    <td>${formatDateTime(item.date)}</td>
                    <td><span class="badges ${statusClass}">${item.status}</span></td>
                    <td>${item.productsSynced || 0}</td>
                    <td>${item.duration || 'N/A'}</td>
                    <td>${item.details || 'N/A'}</td>
                </tr>
            `;
            tableBody.append(row);
        });
    } else {
        tableBody.append('<tr><td colspan="5" class="text-center">No sync history available</td></tr>');
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

// CSS стили для статуса API
const styles = `
<style>
.status-dot {
    display: inline-block;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    margin-right: 8px;
}

.status-dot.connected {
    background-color: #28a745;
}

.status-dot.disconnected {
    background-color: #dc3545;
}

.status-dot.checking {
    background-color: #ffc107;
    animation: pulse 1s infinite;
}

.status-dot.error {
    background-color: #dc3545;
}

@keyframes pulse {
    0% { opacity: 1; }
    50% { opacity: 0.5; }
    100% { opacity: 1; }
}

.sync-progress .progress-step {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}

.sync-progress .step-icon {
    margin-right: 10px;
    color: #ccc;
}

.sync-progress .step-icon .fa-check {
    color: #28a745;
}

.log-container {
    max-height: 200px;
    overflow-y: auto;
    border: 1px solid #ddd;
    padding: 10px;
    background-color: #f8f9fa;
    font-family: monospace;
    font-size: 12px;
}

.log-entry {
    margin-bottom: 5px;
}

.status-card {
    text-align: center;
    padding: 20px;
    border-radius: 8px;
    background-color: #f8f9fa;
}

.status-card .status-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 15px;
    color: white;
    font-size: 24px;
}

.status-card .status-info h6 {
    margin-bottom: 5px;
    color: #666;
}

.status-card .status-info h4 {
    margin: 0;
    font-weight: bold;
}
</style>
`;

// Добавляем стили в head
$('head').append(styles); 