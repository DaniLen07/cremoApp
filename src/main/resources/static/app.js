const money = value => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(value || 0);
const $ = id => document.getElementById(id);
const dashboardCacheKey = 'cremo.dashboard';
const reportCacheKey = 'cremo.weeklyReport';
const requestTimeout = 5000;
let currentPrice = 5000;
let initialStock = 0;
let currentUser = null;

function csrfToken() {
    return document.cookie.split('; ').find(cookie => cookie.startsWith('XSRF-TOKEN='))?.split('=')[1];
}

function setConnectionStatus(online) {
    const status = $('connectionStatus');
    status.classList.toggle('offline', !online);
    status.innerHTML = `<span class="status-dot"></span> ${online ? 'Operación en línea' : 'Servidor desconectado'}`;
}

function wait(milliseconds) { return new Promise(resolve => setTimeout(resolve, milliseconds)); }

async function apiRequest(url, options = {}) {
    const readRequest = !options.method || options.method === 'GET';
    const attempts = readRequest ? 3 : 1;
    let lastError;
    for (let attempt = 0; attempt < attempts; attempt += 1) {
        const controller = new AbortController();
        const timeout = setTimeout(() => controller.abort(), requestTimeout);
        try {
            const headers = { ...(options.headers || {}) };
            if (!readRequest) headers['X-XSRF-TOKEN'] = decodeURIComponent(csrfToken() || '');
            const response = await fetch(url, { ...options, headers, signal: controller.signal });
            clearTimeout(timeout);
            if (response.ok) setConnectionStatus(true);
            if (!response.ok) throw new Error(`Error del servidor (${response.status})`);
            return response;
        } catch (error) {
            clearTimeout(timeout);
            lastError = error.name === 'AbortError' ? new Error('El servidor tardo demasiado en responder.') : error;
            if (attempt < attempts - 1) await wait(500 * (attempt + 1));
        }
    }
    setConnectionStatus(false);
    throw lastError;
}

function readCache(key) {
    try { return JSON.parse(localStorage.getItem(key)); } catch { return null; }
}

function writeCache(key, value) {
    try { localStorage.setItem(key, JSON.stringify(value)); } catch { }
}

function showFeedback(element, message, error = false) {
    element.textContent = message;
    element.classList.toggle('error', error);
}

function updateClock() {
    const now = new Date();
    $('currentDate').textContent = now.toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long' });
    $('currentTime').textContent = now.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
}

function selectedToppings() { return ['arequipe', 'powderedMilk', 'raisins'].filter(id => $(id).checked).length; }
function updateSaleTotal() {
    const quantity = Number($('quantity').value || 0);
    const toppingsTotal = quantity * selectedToppings() * 1000;
    $('saleToppingsTotal').textContent = toppingsTotal ? ` + ${money(toppingsTotal)} en toppings` : '';
    $('saleTotal').textContent = money(quantity * currentPrice + toppingsTotal);
}

function setQuantity(value) {
    $('quantity').value = Math.max(1, Number(value) || 1);
    updateSaleTotal();
}

function renderInventory(inventory) {
    const available = inventory.availableQuantity || 0;
    initialStock = Math.max(inventory.initialQuantity || available, available);
    const percentage = initialStock ? Math.round((available / initialStock) * 100) : 0;
    $('inventoryValue').textContent = available;
    $('inventoryInput').value = available;
    $('stockPercent').textContent = `${percentage}%`;
    $('stockBar').style.width = `${percentage}%`;
    $('stockMessage').textContent = available ? `${available} unidades listas para vender.` : 'No hay unidades disponibles hoy.';
}

function renderSales(sales) {
    const table = $('salesTable');
    if (!sales.length) { table.innerHTML = '<tr><td colspan="6" class="empty-state">Aún no hay ventas registradas.</td></tr>'; return; }
    table.innerHTML = sales.map(sale => `<tr><td>${sale.saleDate}</td><td>${String(sale.createdAt).split('T')[1]?.slice(0, 8) || '--:--:--'}</td><td>${sale.sellerName || 'No especificado'}</td><td>${sale.quantity}</td><td>${sale.paymentMethod}</td><td>${money(sale.total)}</td></tr>`).join('');
}

function renderDailySales(sales) {
    const table = $('dailySalesTable');
    if (!sales.length) {
        table.innerHTML = '<tr><td colspan="6" class="empty-state">Aún no hay ventas registradas hoy.</td></tr>';
        return;
    }
    table.innerHTML = sales.map(sale => `<tr><td>${sale.saleDate}</td><td>${String(sale.createdAt).split('T')[1]?.slice(0, 8) || '--:--:--'}</td><td>${sale.sellerName || 'No especificado'}</td><td>${sale.quantity}</td><td>${sale.paymentMethod}</td><td>${money(sale.total)}</td></tr>`).join('');
}

async function loadDashboard() {
    let data;
    try {
        const response = await apiRequest('/api/dashboard');
        data = await response.json();
        writeCache(dashboardCacheKey, data);
    } catch (error) {
        data = readCache(dashboardCacheKey);
        if (!data) throw error;
    }
    currentPrice = Number(data.product.price);
    $('priceValue').textContent = money(currentPrice);
    $('priceEditorValue').textContent = money(currentPrice);
    $('priceInput').value = currentPrice;
    $('unitsValue').textContent = data.todayUnits || 0;
    $('revenueValue').textContent = money(data.todayTotal);
    renderInventory(data.inventory);
    renderSales(data.recentSales || []);
}

async function loadSellerPrice() {
    const response = await apiRequest('/api/product/current');
    const product = await response.json();
    currentPrice = Number(product.price);
    $('priceValue').textContent = money(currentPrice);
    updateSaleTotal();
}

async function loadSellers() {
    const response = await apiRequest('/api/sellers');
    const sellers = await response.json();
    const stats = currentUser.role === 'ADMIN'
        ? await (await apiRequest('/api/admin/seller-stats')).json()
        : [];
    const select = $('sellerName');
    select.innerHTML = '<option value="" selected disabled>Selecciona un vendedor</option>'
        + sellers.map(seller => `<option value="${seller.name}">${seller.name}</option>`).join('');
    const statsByName = new Map(stats.map(item => [item.sellerName, item]));
    $('sellerList').innerHTML = sellers.length
        ? `<div class="seller-table-wrap"><table><thead><tr><th>Vendedor</th><th>Contacto</th><th>Unidades hoy</th><th>Total vendido hoy</th><th>Acciones</th></tr></thead><tbody>${sellers.map(seller => { const item = statsByName.get(seller.name) || {}; return `<tr><td><strong>${seller.name}</strong><small>${seller.username}</small></td><td>${seller.phone}</td><td>${item.units || 0}</td><td>${money(item.total)}</td><td class="seller-actions"><button type="button" class="seller-action-button edit-seller" data-id="${seller.id}">Editar</button><button type="button" class="seller-action-button delete-seller" data-id="${seller.id}" data-name="${seller.name}">Eliminar</button></td></tr>`; }).join('')}</tbody></table></div>`
        : '<p class="empty-state">Aún no hay vendedores registrados.</p>';
}

async function loadReport() {
    let report;
    try {
        const response = await apiRequest('/api/reports/weekly');
        report = await response.json();
        writeCache(reportCacheKey, report);
    } catch (error) {
        report = readCache(reportCacheKey);
        if (!report) throw error;
    }
    $('reportPeriod').textContent = `${report.start} / ${report.end}`;
    $('reportUnits').textContent = report.units;
    $('reportTotal').textContent = money(report.total);
}

async function loadDailyReport() {
    const response = await apiRequest('/api/reports/daily');
    const report = await response.json();
    $('dailyPeriod').textContent = report.date;
    $('dailyReportDate').textContent = report.date;
    $('dailyUnits').textContent = report.units;
    $('dailyTotal').textContent = money(report.total);
    renderDailySales(report.sales || []);
}

async function refresh() {
    const results = await Promise.allSettled([loadDashboard(), loadDailyReport(), loadReport(), loadSellers()]);
    updateSaleTotal();
    if (results.some(result => result.status === 'rejected')) {
        showFeedback($('saleFeedback'), 'Sin conexion. Mostrando los ultimos datos guardados.', true);
    }
}

$('logoutButton').addEventListener('click', async () => {
    try { await apiRequest('/api/auth/logout', { method: 'POST' }); } finally { window.location.replace('/login.html'); }
});

$('sellerForm').addEventListener('submit', async event => {
    event.preventDefault();
    try {
        await apiRequest('/api/sellers', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name: $('sellerRegisterName').value.trim(),
                phone: $('sellerPhone').value.trim(),
                username: $('sellerUsername').value.trim(),
                password: $('sellerPassword').value
            })
        });
        event.target.reset();
        showFeedback($('sellerFeedback'), 'Vendedor registrado correctamente.');
        await loadSellers();
    } catch (error) {
        showFeedback($('sellerFeedback'), error.message, true);
    }
});

$('sellerList').addEventListener('click', event => {
    const editButton = event.target.closest('.edit-seller');
    const deleteButton = event.target.closest('.delete-seller');
    if (editButton) {
        const row = editButton.closest('tr');
        $('sellerEditId').value = editButton.dataset.id;
        $('sellerEditName').value = row.cells[0].querySelector('strong').textContent;
        $('sellerEditUsername').value = row.cells[0].querySelector('small').textContent;
        $('sellerEditPhone').value = row.cells[1].textContent;
        $('sellerEditPassword').value = '';
        $('sellerEditFeedback').textContent = '';
        $('sellerEditForm').hidden = false;
        $('sellerEditForm').scrollIntoView({ behavior: 'smooth', block: 'center' });
    } else if (deleteButton && window.confirm(`¿Eliminar a ${deleteButton.dataset.name}?`)) {
        apiRequest(`/api/sellers/${deleteButton.dataset.id}`, { method: 'DELETE' })
            .then(loadSellers)
            .catch(error => showFeedback($('sellerFeedback'), error.message, true));
    }
});

$('cancelSellerEdit').addEventListener('click', () => { $('sellerEditForm').hidden = true; });
$('sellerEditForm').addEventListener('submit', async event => {
    event.preventDefault();
    try {
        await apiRequest(`/api/sellers/${$('sellerEditId').value}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: $('sellerEditName').value.trim(), phone: $('sellerEditPhone').value.trim(), username: $('sellerEditUsername').value.trim(), password: $('sellerEditPassword').value })
        });
        $('sellerEditForm').hidden = true;
        showFeedback($('sellerFeedback'), 'Vendedor actualizado correctamente.');
        await loadSellers();
    } catch (error) { showFeedback($('sellerEditFeedback'), error.message, true); }
});

$('decreaseQuantity').addEventListener('click', event => { setQuantity(Number($('quantity').value) - 1); event.currentTarget.classList.add('is-active'); setTimeout(() => event.currentTarget.classList.remove('is-active'), 150); });
$('increaseQuantity').addEventListener('click', event => { setQuantity(Number($('quantity').value) + 1); event.currentTarget.classList.add('is-active'); setTimeout(() => event.currentTarget.classList.remove('is-active'), 150); });
$('quantity').addEventListener('input', updateSaleTotal);
['arequipe', 'powderedMilk', 'raisins'].forEach(id => $(id).addEventListener('change', updateSaleTotal));
$('saleForm').addEventListener('submit', async event => {
    event.preventDefault();
    const feedback = $('saleFeedback');
    const quantity = Number($('quantity').value);
    const sellerName = $('sellerName').value.trim();
    const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;
    try {
        await apiRequest('/api/sales', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ quantity, paymentMethod, sellerName, arequipe: $('arequipe').checked, powderedMilk: $('powderedMilk').checked, raisins: $('raisins').checked }) });
        showFeedback(feedback, 'Venta registrada correctamente.'); $('quantity').value = 1; $('sellerName').value = ''; $('arequipe').checked = false; $('powderedMilk').checked = false; $('raisins').checked = false; updateSaleTotal(); await refresh();
    } catch (error) {
        showFeedback(feedback, `${error.message} La venta no fue registrada.`, true);
    }
});

$('inventoryForm').addEventListener('submit', async event => {
    event.preventDefault();
    try {
        await apiRequest('/api/inventory/today', { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ quantity: Number($('inventoryInput').value) }) });
        showFeedback($('inventoryFeedback'), 'Inventario actualizado.');
        await refresh();
    } catch (error) { showFeedback($('inventoryFeedback'), error.message, true); }
});

$('priceForm').addEventListener('submit', async event => {
    event.preventDefault();
    try {
        await apiRequest('/api/product/price', { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ price: Number($('priceInput').value) }) });
        showFeedback($('inventoryFeedback'), 'Precio actualizado.');
        await refresh();
    } catch (error) { showFeedback($('inventoryFeedback'), error.message, true); }
});

$('refreshButton').addEventListener('click', refresh);
window.addEventListener('online', refresh);
updateClock();
apiRequest('/api/auth/me')
    .then(response => response.json())
    .then(async user => {
        currentUser = user;
        if (currentUser.role !== 'ADMIN') window.location.replace('/seller.html');
        else await refresh();
    })
    .catch(() => window.location.replace('/login.html'));
setInterval(updateClock, 30000);
setInterval(() => { if (currentUser) refresh(); }, 15000);