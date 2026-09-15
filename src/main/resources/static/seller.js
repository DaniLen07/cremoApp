const money = value => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(value || 0);
const $ = id => document.getElementById(id);
let currentPrice = 5000;
let inventoryStock = { arequipeQuantity: 0, powderedMilkQuantity: 0, raisinsQuantity: 0 };

function csrfToken() {
    return document.cookie.split('; ').find(cookie => cookie.startsWith('XSRF-TOKEN='))?.split('=')[1];
}

async function apiRequest(url, options = {}) {
    const writeRequest = options.method && options.method !== 'GET';
    if (writeRequest) await fetch('/api/auth/csrf');
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (writeRequest) headers['X-XSRF-TOKEN'] = decodeURIComponent(csrfToken() || '');
    const response = await fetch(url, { ...options, headers });
    if (response.status === 401) { window.location.replace('/login.html'); throw new Error('Sesión expirada'); }
    if (response.status === 403) throw new Error('No tienes permisos para realizar esta acción.');
    if (!response.ok) {
        let detail = '';
        try {
            const body = await response.json();
            detail = body.detail || body.message || body.error || '';
        } catch { }
        throw new Error(detail || `Error del servidor (${response.status})`);
    }
    return response;
}

function updateClock() { const now = new Date(); $('currentDate').textContent = now.toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long' }); $('currentTime').textContent = now.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' }); }
function selectedToppings() {
    return ['arequipe', 'powderedMilk', 'raisins']
        .reduce((sum, id) => sum + (Number($(id).value) || 0), 0);
}
function validateSelectedToppings() {
    const arequipe = Number($('arequipe').value) || 0;
    const powderedMilk = Number($('powderedMilk').value) || 0;
    const raisins = Number($('raisins').value) || 0;
    const selectedTypes = [arequipe, powderedMilk, raisins].filter(value => value > 0).length;
    if (selectedTypes > 3) {
        return 'Una venta puede tener máximo 3 tipos de toppings.';
    }
    if (arequipe > inventoryStock.arequipeQuantity) {
        return `No hay suficiente arequipe disponible hoy (${inventoryStock.arequipeQuantity}).`;
    }
    if (powderedMilk > inventoryStock.powderedMilkQuantity) {
        return `No hay suficiente leche en polvo disponible hoy (${inventoryStock.powderedMilkQuantity}).`;
    }
    if (raisins > inventoryStock.raisinsQuantity) {
        return `No hay suficientes uvas pasas disponibles hoy (${inventoryStock.raisinsQuantity}).`;
    }
    return null;
}
function updateTotal() { const quantity = Number($('quantity').value || 0); const toppingsQty = selectedToppings(); const toppingsTotal = quantity * toppingsQty * 500; $('saleToppingsTotal').textContent = toppingsQty ? ` + ${money(toppingsTotal)} en toppings` : ''; $('saleTotal').textContent = money(quantity * currentPrice + toppingsTotal); }
function updateToppingButton(inputId) {
    const input = $(inputId);
    const button = document.querySelector(`[data-input="${inputId}"]`);
    const count = $(`${inputId}Count`);
    const value = Number(input.value) || 0;
    count.textContent = value;
    button.classList.toggle('is-selected', value > 0);
}
function setToppingValue(inputId, value) {
    $(inputId).value = Math.max(0, value);
    updateToppingButton(inputId);
    updateTotal();
}
function toppingsSummary(sale) { const parts = []; if (Number(sale.arequipe) > 0) parts.push(`Arequipe: ${sale.arequipe}`); if (Number(sale.powderedMilk) > 0) parts.push(`Leche: ${sale.powderedMilk}`); if (Number(sale.raisins) > 0) parts.push(`Pasa: ${sale.raisins}`); return parts.length ? parts.join(' · ') : 'Sin toppings'; }
function renderSales(sales) { $('sellerSales').innerHTML = sales.length ? `<table><thead><tr><th>Fecha</th><th>Hora</th><th>Cantidad</th><th>Medio de pago</th><th>Toppings</th><th>Total</th><th>Acciones</th></tr></thead><tbody>${sales.map(sale => `<tr><td>${sale.saleDate}</td><td>${String(sale.createdAt).split('T')[1]?.slice(0, 8) || '--:--:--'}</td><td>${sale.quantity}</td><td>${sale.paymentMethod}</td><td>${toppingsSummary(sale)}</td><td>${money(sale.total)}</td><td><button type="button" class="seller-action-button edit-sale" data-id="${sale.id}">Editar</button><button type="button" class="seller-action-button delete-seller" data-id="${sale.id}">Eliminar</button></td></tr>`).join('')}</tbody></table>` : '<p class="empty-state">Aún no tienes ventas registradas.</p>'; }
async function load() { const user = await (await apiRequest('/api/auth/me')).json(); if (user.role !== 'SELLER') { window.location.replace('/'); return; } $('userLabel').textContent = `${user.username} · Vendedor`; const product = await (await apiRequest('/api/product/current')).json(); currentPrice = Number(product.price); const inventory = await (await apiRequest('/api/inventory/today')).json(); inventoryStock = { arequipeQuantity: Number(inventory.arequipeQuantity) || 0, powderedMilkQuantity: Number(inventory.powderedMilkQuantity) || 0, raisinsQuantity: Number(inventory.raisinsQuantity) || 0 }; $('availableProduct').textContent = inventory.availableQuantity || 0; const stats = await (await apiRequest('/api/seller/me/stats')).json(); $('todayUnits').textContent = stats.todayUnits; $('todayTotal').textContent = money(stats.todayTotal); $('totalUnits').textContent = stats.totalUnits; $('totalAmount').textContent = money(stats.totalAmount); renderSales(stats.sales); updateTotal(); }

$('saleForm').addEventListener('submit', async event => { event.preventDefault(); const validationError = validateSelectedToppings(); if (validationError) { $('saleFeedback').textContent = validationError; $('saleFeedback').classList.add('error'); return; } try { await apiRequest('/api/sales', { method: 'POST', body: JSON.stringify({ quantity: Number($('quantity').value), paymentMethod: document.querySelector('input[name="paymentMethod"]:checked').value, sellerName: 'self', arequipe: Number($('arequipe').value) || 0, powderedMilk: Number($('powderedMilk').value) || 0, raisins: Number($('raisins').value) || 0 }) }); $('quantity').value = 1; $('arequipe').value = 0; $('powderedMilk').value = 0; $('raisins').value = 0; $('saleFeedback').textContent = 'Venta registrada correctamente.'; $('saleFeedback').classList.remove('error'); await load(); } catch (error) { $('saleFeedback').textContent = error.message; $('saleFeedback').classList.add('error'); } });

$('sellerSales').addEventListener('click', async event => {
    const editButton = event.target.closest('.edit-sale');
    const deleteButton = event.target.closest('.delete-seller');
    if (editButton) {
        const sale = (await (await apiRequest('/api/seller/me/stats')).json()).sales.find(item => String(item.id) === String(editButton.dataset.id));
        const quantity = Number(prompt('Cantidad de unidades', sale?.quantity ?? 1));
        if (!Number.isFinite(quantity) || quantity < 1) return;
        const arequipe = Number(prompt('Cantidad de arequipe', sale?.arequipe ?? 0));
        const powderedMilk = Number(prompt('Cantidad de leche en polvo', sale?.powderedMilk ?? 0));
        const raisins = Number(prompt('Cantidad de uvas pasas', sale?.raisins ?? 0));
        const paymentMethod = prompt('Medio de pago (EFECTIVO/NEQUI)', sale?.paymentMethod ?? 'EFECTIVO');
        await apiRequest(`/api/sales/${editButton.dataset.id}`, { method: 'PUT', body: JSON.stringify({ quantity, paymentMethod, sellerName: sale?.sellerName || 'self', arequipe, powderedMilk, raisins }) });
        await load();
        return;
    }
    if (deleteButton && window.confirm('¿Eliminar esta venta?')) {
        await apiRequest(`/api/sales/${deleteButton.dataset.id}`, { method: 'DELETE' });
        await load();
    }
});
$('decreaseQuantity').addEventListener('click', event => { $('quantity').value = Math.max(1, Number($('quantity').value) - 1); updateTotal(); event.currentTarget.classList.add('is-active'); setTimeout(() => event.currentTarget.classList.remove('is-active'), 150); }); $('increaseQuantity').addEventListener('click', event => { $('quantity').value = Number($('quantity').value) + 1; updateTotal(); event.currentTarget.classList.add('is-active'); setTimeout(() => event.currentTarget.classList.remove('is-active'), 150); }); $('quantity').addEventListener('input', updateTotal);['arequipe', 'powderedMilk', 'raisins'].forEach(id => { $(id).addEventListener('change', () => { updateToppingButton(id); updateTotal(); }); }); document.querySelectorAll('.topping-button').forEach(button => button.addEventListener('click', () => { const inputId = button.dataset.input; setToppingValue(inputId, Number($(inputId).value) + 1); })); $('logoutButton').addEventListener('click', async () => { try { await apiRequest('/api/auth/logout', { method: 'POST' }); } finally { window.location.replace('/login.html'); } }); updateClock(); setInterval(updateClock, 30000); load().catch(() => window.location.replace('/login.html')); setInterval(() => load().catch(() => { }), 15000);