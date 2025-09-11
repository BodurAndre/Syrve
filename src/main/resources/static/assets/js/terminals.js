$(document).ready(function() {
    // Загрузка терминалов при загрузке страницы
    loadTerminals();
    
    // Обработчики событий
    $('#syncTerminals').on('click', syncTerminals);
    $('#refreshTerminals').on('click', loadTerminals);
    
    // Инициализация DataTable
    initializeDataTable();
});

function loadTerminals() {
    showLoader();
    
    $.ajax({
        url: '/admin/api/terminals',
        method: 'GET',
        success: function(data) {
            updateTerminalsTable(data);
            updateTerminalsStats(data);
            updateTerminalStatusChart(data);
            updateActivityFeed();
            hideLoader();
        },
        error: function(xhr, status, error) {
            console.error('Error loading terminals:', error);
            showNotification('Error loading terminals', false);
            hideLoader();
        }
    });
}

function syncTerminals() {
    if (!confirm('Are you sure you want to sync terminals? This may take a few moments.')) {
        return;
    }
    
    showLoader();
    $('#syncTerminals').prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> Syncing...');
    
    $.ajax({
        url: '/admin/saveTerminalGroup',
        method: 'GET',
        success: function(response) {
            showNotification('Terminals synced successfully!', true);
            loadTerminals(); // Перезагружаем данные
        },
        error: function(xhr, status, error) {
            console.error('Error syncing terminals:', error);
            showNotification('Error syncing terminals: ' + (xhr.responseText || error), false);
        },
        complete: function() {
            $('#syncTerminals').prop('disabled', false).html('<i class="fas fa-sync-alt"></i> Sync Terminals');
            hideLoader();
        }
    });
}

function updateTerminalsTable(terminals) {
    const tableBody = $('#terminalsTableBody');
    tableBody.empty();
    
    if (terminals && terminals.length > 0) {
        terminals.forEach(terminal => {
            const statusClass = terminal.isActive ? 'bg-lightgreen' : 'bg-lightred';
            const statusText = terminal.isActive ? 'Active' : 'Sleep Mode';
            
            const row = `
                <tr>
                    <td>${terminal.terminalId}</td>
                    <td>${terminal.name}</td>
                    <td>${terminal.address || 'N/A'}</td>
                    <td>${terminal.timeZone || 'N/A'}</td>
                    <td><span class="badges ${statusClass}">${statusText}</span></td>
                    <td>${terminal.restaurantInfo ? terminal.restaurantInfo.nameRestaurant : 'N/A'}</td>
                    <td>
                        <div class="actions">
                            <a href="javascript:void(0);" class="btn btn-sm btn-outline-primary" onclick="viewTerminal('${terminal.terminalId}')">
                                <i class="fas fa-eye"></i>
                            </a>
                            <a href="javascript:void(0);" class="btn btn-sm btn-outline-info" onclick="testTerminal('${terminal.terminalId}')">
                                <i class="fas fa-check"></i>
                            </a>
                        </div>
                    </td>
                </tr>
            `;
            tableBody.append(row);
        });
    } else {
        tableBody.append('<tr><td colspan="7" class="text-center">No terminals found</td></tr>');
    }
    
    // Обновляем DataTable
    if ($.fn.DataTable.isDataTable('.datatable')) {
        $('.datatable').DataTable().destroy();
    }
    initializeDataTable();
}

function updateTerminalsStats(terminals) {
    if (!terminals) {
        $('#totalTerminals').text('0');
        $('#activeTerminals').text('0');
        $('#sleepTerminals').text('0');
        return;
    }
    
    const total = terminals.length;
    const active = terminals.filter(t => t.isActive).length;
    const sleep = total - active;
    
    $('#totalTerminals').text(total);
    $('#activeTerminals').text(active);
    $('#sleepTerminals').text(sleep);
    
    // Обновляем время последней синхронизации
    $('#lastSync').text(new Date().toLocaleString('ru-RU'));
}

function updateTerminalStatusChart(terminals) {
    if (!terminals || terminals.length === 0) {
        return;
    }
    
    const active = terminals.filter(t => t.isActive).length;
    const sleep = terminals.length - active;
    
    const ctx = document.getElementById('terminalStatusChart').getContext('2d');
    
    // Уничтожаем предыдущий график если он существует
    if (window.terminalStatusChart) {
        window.terminalStatusChart.destroy();
    }
    
    window.terminalStatusChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Active', 'Sleep Mode'],
            datasets: [{
                data: [active, sleep],
                backgroundColor: ['#28a745', '#ffc107'],
                borderWidth: 2,
                borderColor: '#fff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

function updateActivityFeed() {
    const activityFeed = $('#activityFeed');
    activityFeed.empty();
    
    // Здесь можно добавить реальные данные активности
    const activities = [
        {
            time: '2 minutes ago',
            action: 'Terminal sync completed',
            status: 'success'
        },
        {
            time: '5 minutes ago',
            order: 'Order #123 processed',
            status: 'info'
        },
        {
            time: '10 minutes ago',
            action: 'Terminal status check',
            status: 'warning'
        }
    ];
    
    activities.forEach(activity => {
        const activityItem = `
            <div class="activity-item">
                <div class="activity-content">
                    <div class="activity-info">
                        <span class="activity-time">${activity.time}</span>
                        <span class="activity-text">${activity.action || activity.order}</span>
                    </div>
                    <span class="activity-status ${activity.status}"></span>
                </div>
            </div>
        `;
        activityFeed.append(activityItem);
    });
}

function initializeDataTable() {
    $('.datatable').DataTable({
        "pageLength": 10,
        "order": [[0, "asc"]],
        "language": {
            "search": "Search:",
            "lengthMenu": "Show _MENU_ entries",
            "info": "Showing _START_ to _END_ of _TOTAL_ entries",
            "infoEmpty": "Showing 0 to 0 of 0 entries",
            "infoFiltered": "(filtered from _MAX_ total entries)",
            "emptyTable": "No data available in table"
        }
    });
}

function viewTerminal(terminalId) {
    // Здесь можно добавить модальное окно для просмотра деталей терминала
    alert('View terminal details for: ' + terminalId);
}

function testTerminal(terminalId) {
    showLoader();
    
    $.ajax({
        url: '/admin/api/terminals/' + terminalId + '/test',
        method: 'POST',
        success: function(response) {
            showNotification('Terminal test completed successfully', true);
        },
        error: function(xhr, status, error) {
            showNotification('Terminal test failed: ' + (xhr.responseText || error), false);
        },
        complete: function() {
            hideLoader();
        }
    });
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