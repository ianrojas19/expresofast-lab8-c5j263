const API_URL = 'http://localhost:8080/api';

let enviosGlobal = [];
let currentFilter = 'TODOS';

// Decode JWT to get info
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch(e) {
        return null;
    }
}

// Authentication Wrapper
async function fetchWithAuth(url, options = {}) {
    const token = sessionStorage.getItem('jwt_token');
    if (!token) {
        window.location.href = 'index.html';
        throw new Error("No token");
    }

    if (!options.headers) options.headers = {};
    options.headers['Authorization'] = `Bearer ${token}`;
    options.headers['Content-Type'] = 'application/json';

    const response = await fetch(url, options);

    if (response.status === 401 || response.status === 403) {
        sessionStorage.removeItem('jwt_token');
        window.location.href = 'index.html';
        throw new Error('No autorizado');
    }

    if (response.status === 400) {
        const errJson = await response.json();
        showErrorModal(errJson);
        throw new Error('Bad Request');
    }

    return response;
}

function showErrorModal(err) {
    const errorList = document.getElementById('error-list');
    if (errorList) {
        errorList.innerHTML = `<p>${err.detail || 'Ocurrió un error en la solicitud.'}</p>`;
        if (err.errors && Array.isArray(err.errors)) {
            const ul = document.createElement('ul');
            err.errors.forEach(e => {
                const li = document.createElement('li');
                li.innerText = e;
                ul.appendChild(li);
            });
            errorList.appendChild(ul);
        }
        document.getElementById('errorModal').style.display = 'block';
    }
}

// Check which page we are on
document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const isDashboard = document.getElementById('enviosGrid') !== null;

    if (loginForm) {
        initLogin();
    } else if (isDashboard) {
        initDashboard();
    }
});

function initLogin() {
    const form = document.getElementById('loginForm');
    const errorDiv = document.getElementById('login-error');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            username: form.username.value,
            password: form.password.value
        };

        try {
            const res = await fetch(`${API_URL}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                const data = await res.json();
                sessionStorage.setItem('jwt_token', data.token);
                window.location.href = 'dashboard.html';
            } else {
                errorDiv.innerText = 'Credenciales inválidas';
                errorDiv.style.display = 'block';
            }
        } catch (err) {
            errorDiv.innerText = 'Error de conexión con el servidor.';
            errorDiv.style.display = 'block';
        }
    });
}

function initDashboard() {
    const token = sessionStorage.getItem('jwt_token');
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    const payload = parseJwt(token);
    const roles = payload.roles || [];
    document.getElementById('user-info').innerText = payload.sub;

    document.getElementById('btn-logout').addEventListener('click', () => {
        sessionStorage.removeItem('jwt_token');
        window.location.href = 'index.html';
    });

    // Mobile menu toggle
    const menuToggle = document.getElementById('menu-toggle');
    if (menuToggle) {
        menuToggle.addEventListener('click', () => {
            document.getElementById('nav-filters').classList.toggle('show');
        });
    }

    // Close Modals
    document.getElementById('closeBitacoraModal').onclick = () => document.getElementById('bitacoraModal').style.display = 'none';
    if(document.getElementById('closeErrorModal')) {
        document.getElementById('closeErrorModal').onclick = () => document.getElementById('errorModal').style.display = 'none';
    }
    window.onclick = (e) => {
        if (e.target.classList.contains('modal')) e.target.style.display = 'none';
    };

    // Filters
    document.querySelectorAll('#nav-filters button').forEach(btn => {
        btn.addEventListener('click', (e) => {
            document.querySelectorAll('#nav-filters button').forEach(b => b.classList.remove('active'));
            e.target.classList.add('active');
            currentFilter = e.target.getAttribute('data-filter');
            renderEnvios();
        });
    });

    // Role Based rendering
    if (roles.includes('ROLE_ADMIN')) {
        document.getElementById('panel-admin').style.display = 'block';
    } else if (roles.includes('ROLE_OPERADOR')) {
        document.getElementById('panel-admin').style.display = 'block';
        document.getElementById('btn-nuevo-vehiculo').style.display = 'none'; // Only Admin can add vehicle
    }

    if (roles.includes('ROLE_ADMIN') || roles.includes('ROLE_OPERADOR')) {
        const formEnvio = document.getElementById('form-envio');
        if (formEnvio) {
            formEnvio.addEventListener('submit', async (e) => {
                e.preventDefault();
                const data = {
                    codigoRastreo: formEnvio.codigoRastreo.value,
                    direccionDestino: formEnvio.direccionDestino.value,
                    pesoKg: parseFloat(formEnvio.pesoKg.value),
                    costo: parseFloat(formEnvio.costo.value),
                    vehiculoId: parseInt(formEnvio.vehiculoId.value),
                    conductorId: parseInt(formEnvio.conductorId.value)
                };
                try {
                    const res = await fetchWithAuth(`${API_URL}/envios`, {
                        method: 'POST',
                        body: JSON.stringify(data)
                    });
                    if (res.ok) {
                        alert('Envío registrado con éxito');
                        formEnvio.reset();
                        cargarEnvios();
                    }
                } catch (e) {
                    // Handled by fetchWithAuth Error Modal
                }
            });
        }
    }

    cargarEnvios();
}

async function cargarEnvios() {
    try {
        const res = await fetchWithAuth(`${API_URL}/envios/optimizados`);
        if (res.ok) {
            enviosGlobal = await res.json();
            updateKPIs();
            renderEnvios();
        }
    } catch (error) {
        console.error(error);
    }
}

function updateKPIs() {
    document.getElementById('kpi-total').innerText = enviosGlobal.length;
    document.getElementById('kpi-entregados').innerText = enviosGlobal.filter(e => e.estadoEnvio === 'ENTREGADO').length;
    const vehiculos = new Set();
    enviosGlobal.forEach(e => { if (e.placaVehiculo) vehiculos.add(e.placaVehiculo); });
    document.getElementById('kpi-vehiculos').innerText = vehiculos.size;
}

function renderEnvios() {
    const grid = document.getElementById('enviosGrid');
    grid.innerHTML = '';
    
    const token = sessionStorage.getItem('jwt_token');
    const roles = parseJwt(token).roles || [];

    const filtrados = currentFilter === 'TODOS' ? enviosGlobal : enviosGlobal.filter(e => e.estadoEnvio === currentFilter);

    filtrados.forEach(envio => {
        const article = document.createElement('article');
        
        let actionButtons = '';
        if (roles.includes('ROLE_ADMIN') || roles.includes('ROLE_OPERADOR')) {
            if (envio.estadoEnvio === 'PENDIENTE') {
                actionButtons += `<button class="btn-secondary" onclick="actualizarEstado(${envio.id}, 'EN_TRANSITO')">Marcar en Tránsito</button>`;
            }
            actionButtons += `<button class="btn-secondary" onclick="verBitacora(${envio.id})">Ver Bitácora</button>`;
        }
        
        if (roles.includes('ROLE_CONDUCTOR') || roles.includes('ROLE_ADMIN')) {
            if (envio.estadoEnvio === 'EN_TRANSITO') {
                actionButtons += `<button class="btn-primary" onclick="actualizarEstado(${envio.id}, 'ENTREGADO')">Marcar Entregado</button>`;
            }
        }

        article.innerHTML = `
            <div class="card-header">
                <h4>${envio.codigoRastreo}</h4>
                <span class="badge status-${envio.estadoEnvio}">${envio.estadoEnvio.replace('_', ' ')}</span>
            </div>
            <div class="card-body">
                <p><strong>Destino:</strong> ${envio.direccionDestino}</p>
                <p><strong>Peso/Costo:</strong> ${envio.pesoKg}kg - ₡${envio.costo}</p>
                <p><strong>Vehículo:</strong> ${envio.placaVehiculo || 'N/A'}</p>
                <p><strong>Conductor:</strong> ${envio.nombreConductor || 'N/A'}</p>
            </div>
            <div class="card-actions">
                ${actionButtons}
            </div>
        `;
        grid.appendChild(article);
    });
}

window.actualizarEstado = async function(id, nuevoEstado) {
    const obs = prompt("Ingrese observaciones para el cambio de estado:");
    if (obs === null) return;

    try {
        const res = await fetchWithAuth(`${API_URL}/envios/${id}/estado`, {
            method: 'PATCH',
            body: JSON.stringify({ nuevoEstado: nuevoEstado, observaciones: obs })
        });
        if (res.ok) {
            cargarEnvios();
        }
    } catch (e) {
        // Handled
    }
}

window.verBitacora = async function(id) {
    try {
        const res = await fetchWithAuth(`${API_URL}/envios/${id}/bitacora`);
        if (res.ok) {
            const bitacora = await res.json();
            const list = document.getElementById('bitacora-list');
            if (bitacora.length === 0) {
                list.innerHTML = '<p>No hay eventos registrados para este envío.</p>';
            } else {
                let html = '<table><tr><th>Fecha</th><th>Usuario</th><th>Cambio</th><th>Observaciones</th></tr>';
                bitacora.forEach(b => {
                    html += `<tr>
                        <td>${new Date(b.fechaCambio).toLocaleString()}</td>
                        <td>${b.usuario}</td>
                        <td>${b.estadoAnterior} &rarr; ${b.estadoNuevo}</td>
                        <td>${b.observaciones || ''}</td>
                    </tr>`;
                });
                html += '</table>';
                list.innerHTML = html;
            }
            document.getElementById('bitacoraModal').style.display = 'block';
        }
    } catch (e) {
        // Handled
    }
}
