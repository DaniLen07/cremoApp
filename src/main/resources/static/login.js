function csrfToken() {
    return document.cookie.split('; ').find(cookie => cookie.startsWith('XSRF-TOKEN='))?.split('=')[1];
}

const loginRequest = async (url, options = {}) => {
    if (options.method && options.method !== 'GET') await fetch('/api/auth/csrf');
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (options.method && options.method !== 'GET') headers['X-XSRF-TOKEN'] = decodeURIComponent(csrfToken() || '');
    const response = await fetch(url, { ...options, headers });
    if (!response.ok) throw new Error('Credenciales incorrectas');
    return response;
};

document.getElementById('loginForm').addEventListener('submit', async event => {
    event.preventDefault();
    const feedback = document.getElementById('loginFeedback');
    try {
        const response = await loginRequest('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({
                username: document.getElementById('loginUsername').value.trim(),
                password: document.getElementById('loginPassword').value
            })
        });
        const user = await response.json();
        window.location.replace(user.role === 'ADMIN' ? '/' : '/seller.html');
    } catch (error) {
        feedback.textContent = 'Usuario o contraseña incorrectos.';
        feedback.classList.add('error');
    }
});

fetch('/api/auth/me').then(response => {
    if (response.ok) return response.json();
    throw new Error('No hay sesión');
}).then(user => window.location.replace(user.role === 'ADMIN' ? '/' : '/seller.html')).catch(() => { });