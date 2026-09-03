const money = value => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(value || 0);
const $ = id => document.getElementById(id);
let currentPrice = 5000;

function csrfToken() {
    return document.cookie.split('; ').find(cookie => cookie.startsWith('XSRF-TOKEN='))?.split('=')[1];
}

async function apiRequest(url, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (options.method && options.method !== 'GET') headers['X-XSRF-TOKEN'] = decodeURIComponent(csrfToken() || '');
    const response = await fetch(url, { ...options, headers });
    if (response.status === 401 || response.status === 403) { window.location.replace('/login.html'); throw new Error('Sesión expirada'); }
    if (!response.ok) throw new Error('No se pudo completar la operación');
    return response;
}

function updateClock() { const now = new Date(); $('currentDate').textContent = now.toLocaleDateString('es-CO', { weekday: 'long', day: 'numeric', month: 'long' }); $('currentTime').textContent = now.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' }); }
function selectedToppings() { return ['arequipe', 'powderedMilk', 'raisins'].filter(id => $(id).checked).length; }
function updateTotal() { const quantity = Number($('quantity').value || 0); const toppingsTotal = quantity * selectedToppings() * 1000; $('saleToppingsTotal').textContent = toppingsTotal ? ` + ${money(toppingsTotal)} en toppings` : ''; $('saleTotal').textContent = money(quantity * currentPrice + toppingsTotal); }
function renderSales(sales) { $('sellerSales').innerHTML = sales.length ? `<table><thead><tr><th>Fecha</th><th>Hora</th><th>Cantidad</th><th>Medio de pago</th><th>Total</th></tr></thead><tbody>${sales.map(sale => `<tr><td>${sale.saleDate}</td><td>${String(sale.createdAt).split('T')[1]?.slice(0, 8) || '--:--:--'}</td><td>${sale.quantity}</td><td>${sale.paymentMethod}</td><td>${money(sale.total)}</td></tr>`).join('')}</tbody></table>` : '<p class="empty-state">Aún no tienes ventas registradas.</p>'; }
async function load() { const user = await (await apiRequest('/api/auth/me')).json(); if (user.role !== 'SELLER') { window.location.replace('/'); return; } $('userLabel').textContent = `${user.username} · Vendedor`; const product = await (await apiRequest('/api/product/current')).json(); currentPrice = Number(product.price); const inventory = await (await apiRequest('/api/inventory/today')).json(); $('availableProduct').textContent = inventory.availableQuantity || 0; const stats = await (await apiRequest('/api/seller/me/stats')).json(); $('todayUnits').textContent = stats.todayUnits; $('todayTotal').textContent = money(stats.todayTotal); $('totalUnits').textContent = stats.totalUnits; $('totalAmount').textContent = money(stats.totalAmount); renderSales(stats.sales); updateTotal(); }

$('saleForm').addEventListener('submit', async event => { event.preventDefault(); try { await apiRequest('/api/sales', { method: 'POST', body: JSON.stringify({ quantity: Number($('quantity').value), paymentMethod: document.querySelector('input[name="paymentMethod"]:checked').value, sellerName: 'self', arequipe: $('arequipe').checked, powderedMilk: $('powderedMilk').checked, raisins: $('raisins').checked }) }); $('quantity').value = 1; $('arequipe').checked = false; $('powderedMilk').checked = false; $('raisins').checked = false; $('saleFeedback').textContent = 'Venta registrada correctamente.'; await load(); } catch (error) { $('saleFeedback').textContent = error.message; $('saleFeedback').classList.add('error'); } });
$('decreaseQuantity').addEventListener('click', event => { $('quantity').value = Math.max(1, Number($('quantity').value) - 1); updateTotal(); event.currentTarget.classList.add('is-active'); setTimeout(() => event.currentTarget.classList.remove('is-active'), 150); }); $('increaseQuantity').addEventListener('click', event => { $('quantity').value = Number($('quantity').value) + 1; updateTotal(); event.currentTarget.classList.add('is-active'); setTimeout(() => event.currentTarget.classList.remove('is-active'), 150); }); $('quantity').addEventListener('input', updateTotal);['arequipe', 'powderedMilk', 'raisins'].forEach(id => $(id).addEventListener('change', updateTotal)); $('logoutButton').addEventListener('click', async () => { try { await apiRequest('/api/auth/logout', { method: 'POST' }); } finally { window.location.replace('/login.html'); } }); updateClock(); setInterval(updateClock, 30000); load().catch(() => window.location.replace('/login.html')); setInterval(() => load().catch(() => { }), 15000);