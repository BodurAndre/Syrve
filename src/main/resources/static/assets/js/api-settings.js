$(document).ready(function() {
    // Загрузка текущих настроек
    loadCurrentSettings();
    
    // Проверка статуса соединения
    checkConnectionStatus();
    
    // Обработчики событий
    $('#apiProvider').on('change', updateApiBaseUrl);
    $('#testConnection').on('click', testApiConnection);
    $('#saveSettings').on('click', saveApiSettings);
    $('#resetToken').on('click', resetApiToken);
    
    // Обновление статуса каждые 30 секунд
    setInterval(function() {
        checkConnectionStatus();
    }, 30000);
});

function loadCurrentSettings() {
    $.ajax({
        url: '/admin/viewCompany',
        method: 'GET',
        success: function(data) {
            if (data) {
                $('#apiLogin').val(data.apiLogin || '');
                $('#apiProvider').val(data.sectorRestaurant || 'Syrve');
                updateApiBaseUrl();
            }
        },
        error: function() {
            console.log('Error loading current settings');
        }
    });
}

function updateApiBaseUrl() {
    const provider = $('#apiProvider').val();
    let baseUrl = '';
    
    switch(provider) {
        case 'IIKO':
            baseUrl = 'https://api-ru.iiko.services/api/';
            break;
        case 'Syrve':
        default:
            baseUrl = 'https://api-eu.syrve.live/api/';
            break;
    }
    
    $('#apiBaseUrl').val(baseUrl);
}

function testApiConnection() {
    const apiLogin = $('#apiLogin').val();
    const provider = $('#apiProvider').val();
    
    if (!apiLogin) {
        showNotification('Please enter API Login', false);
        return;
    }
    
    $('#testConnection').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Testing...');
    $('#apiStatus').text('Testing...').removeClass().addClass('status-badge testing');
    
    const startTime = Date.now();
    
    $.ajax({
        url: '/admin/getToken',
        method: 'GET',
        success: function(response) {
            const responseTime = Date.now() - startTime;
            $('#apiStatus').text('Connected').removeClass().addClass('status-badge connected');
            $('#tokenStatus').text('Valid');
            $('#responseTime').text(responseTime + 'ms');
            $('#lastTest').text(new Date().toLocaleString('ru-RU'));
            
            addApiLog('API connection test successful', 'success');
            showNotification('API connection test successful', true);
        },
        error: function(xhr, status, error) {
            const responseTime = Date.now() - startTime;
            $('#apiStatus').text('Failed').removeClass().addClass('status-badge failed');
            $('#tokenStatus').text('Invalid');
            $('#responseTime').text(responseTime + 'ms');
            $('#lastTest').text(new Date().toLocaleString('ru-RU'));
            
            const errorMessage = xhr.responseText || 'Connection failed';
            addApiLog('API connection test failed: ' + errorMessage, 'error');
            showNotification('API connection test failed: ' + errorMessage, false);
        },
        complete: function() {
            $('#testConnection').prop('disabled', false).html('<i class="fas fa-plug"></i> Test Connection');
        }
    });
}

function saveApiSettings() {
    const apiLogin = $('#apiLogin').val();
    const provider = $('#apiProvider').val();
    
    if (!apiLogin) {
        showNotification('Please enter API Login', false);
        return;
    }
    
    $('#saveSettings').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Saving...');
    
    $.ajax({
        url: '/admin/updateApiLogin',
        method: 'POST',
        contentType: 'application/x-www-form-urlencoded',
        data: {
            apiLogin: apiLogin,
            sectorRestaurant: provider
        },
        success: function(response) {
            addApiLog('API settings saved successfully', 'success');
            showNotification('API settings saved successfully', true);
            
            // Обновляем статус
            setTimeout(function() {
                checkConnectionStatus();
            }, 1000);
        },
        error: function(xhr, status, error) {
            const errorMessage = xhr.responseText || 'Failed to save settings';
            addApiLog('Failed to save API settings: ' + errorMessage, 'error');
            showNotification('Failed to save API settings: ' + errorMessage, false);
        },
        complete: function() {
            $('#saveSettings').prop('disabled', false).html('<i class="fas fa-save"></i> Save Settings');
        }
    });
}

function resetApiToken() {
    if (!confirm('Are you sure you want to reset the API token? This will invalidate the current token.')) {
        return;
    }
    
    $('#resetToken').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Resetting...');
    
    $.ajax({
        url: '/admin/resetToken',
        method: 'POST',
        success: function(response) {
            addApiLog('API token reset successfully', 'success');
            showNotification('API token reset successfully', true);
            
            // Обновляем статус
            $('#tokenStatus').text('Reset');
            setTimeout(function() {
                checkConnectionStatus();
            }, 1000);
        },
        error: function(xhr, status, error) {
            const errorMessage = xhr.responseText || 'Failed to reset token';
            addApiLog('Failed to reset API token: ' + errorMessage, 'error');
            showNotification('Failed to reset API token: ' + errorMessage, false);
        },
        complete: function() {
            $('#resetToken').prop('disabled', false).html('<i class="fas fa-refresh"></i> Reset Token');
        }
    });
}

function checkConnectionStatus() {
    $.ajax({
        url: '/admin/api/system/status',
        method: 'GET',
        success: function(data) {
            if (data.apiConnection) {
                $('#apiStatus').text('Connected').removeClass().addClass('status-badge connected');
                $('#tokenStatus').text('Valid');
            } else {
                $('#apiStatus').text('Disconnected').removeClass().addClass('status-badge disconnected');
                $('#tokenStatus').text('Invalid');
            }
        },
        error: function() {
            $('#apiStatus').text('Error').removeClass().addClass('status-badge error');
            $('#tokenStatus').text('Unknown');
        }
    });
}

function addApiLog(message, type = 'info') {
    const timestamp = new Date().toLocaleString('ru-RU');
    const logClass = type === 'error' ? 'text-danger' : type === 'success' ? 'text-success' : 'text-info';
    
    const logEntry = `
        <div class="log-entry ${logClass}">
            <small>[${timestamp}]</small> ${message}
        </div>
    `;
    
    $('#apiLog').prepend(logEntry);
    
    // Ограничиваем количество записей в логе
    const logEntries = $('#apiLog .log-entry');
    if (logEntries.length > 50) {
        logEntries.slice(50).remove();
    }
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
.status-badge {
    display: inline-block;
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: bold;
    text-transform: uppercase;
}

.status-badge.connected {
    background-color: #d4edda;
    color: #155724;
    border: 1px solid #c3e6cb;
}

.status-badge.disconnected {
    background-color: #f8d7da;
    color: #721c24;
    border: 1px solid #f5c6cb;
}

.status-badge.testing {
    background-color: #fff3cd;
    color: #856404;
    border: 1px solid #ffeaa7;
    animation: pulse 1s infinite;
}

.status-badge.failed {
    background-color: #f8d7da;
    color: #721c24;
    border: 1px solid #f5c6cb;
}

.status-badge.error {
    background-color: #f8d7da;
    color: #721c24;
    border: 1px solid #f5c6cb;
}

@keyframes pulse {
    0% { opacity: 1; }
    50% { opacity: 0.5; }
    100% { opacity: 1; }
}

.status-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    padding: 8px 0;
    border-bottom: 1px solid #eee;
}

.status-item:last-child {
    border-bottom: none;
}

.endpoint-list {
    font-family: monospace;
    font-size: 12px;
}

.endpoint-item {
    margin-bottom: 8px;
    padding: 4px 0;
}

.endpoint-item code {
    background-color: #f8f9fa;
    padding: 2px 4px;
    border-radius: 3px;
    color: #e83e8c;
}

.api-log {
    max-height: 300px;
    overflow-y: auto;
    border: 1px solid #ddd;
    padding: 10px;
    background-color: #f8f9fa;
    font-family: monospace;
    font-size: 12px;
}

.log-entry {
    margin-bottom: 5px;
    padding: 2px 0;
}

.log-entry:first-child {
    border-bottom: 1px solid #ddd;
    padding-bottom: 5px;
    margin-bottom: 10px;
}
</style>
`;

// Добавляем стили в head
$('head').append(styles); 