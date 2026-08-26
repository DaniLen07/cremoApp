const money = value => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(value || 0);
const $ = id => document.getElementById(id);
const dashboardCacheKey = 'cremo.dashboard';
const reportCacheKey = 'cremo.weeklyReport';
const requestTimeout = 5000;
let currentPrice = 5000;
let initialStock = 0;

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
            const response = await fetch(url, { ...options, signal: controller.signal });
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

function updateSaleTotal() { $('saleTotal').textContent = money(Number($('quantity').value || 0) * currentPrice); }

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
    table.innerHTML = sales.map(sale => `<tr><td>${sale.saleDate}</td><td>${new Date(sale.createdAt).toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' })}</td><td>${sale.sellerName || 'No especificado'}</td><td>${sale.quantity}</td><td>${sale.paymentMethod}</td><td>${money(sale.total)}</td></tr>`).join('');
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

async function refresh() {
    const results = await Promise.allSettled([loadDashboard(), loadReport()]);
    updateSaleTotal();
    if (results.some(result => result.status === 'rejected')) {
        showFeedback($('saleFeedback'), 'Sin conexion. Mostrando los ultimos datos guardados.', true);
    }
}

$('quantity').addEventListener('input', updateSaleTotal);
$('saleForm').addEventListener('submit', async event => {
    event.preventDefault();
    const feedback = $('saleFeedback');
    const quantity = Number($('quantity').value);
    const sellerName = $('sellerName').value.trim();
    const paymentMethod = document.querySelector('input[name="paymentMethod"]:checked').value;
    try {
        await apiRequest('/api/sales', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ quantity, paymentMethod, sellerName }) });
        showFeedback(feedback, 'Venta registrada correctamente.'); $('quantity').value = 1; $('sellerName').value = ''; updateSaleTotal(); await refresh();
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
updateClock(); setInterval(updateClock, 30000); setInterval(refresh, 15000); refresh();