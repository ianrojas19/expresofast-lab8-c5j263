const API_URL = 'http://localhost:8080/api/envios';
let enviosGlobal = [];
let bitacoraActual = [];

function checkAuth() {
    const token = localStorage.getItem('jwt_token');
    if (!token) {
        window.location.href = 'login.html';
    }
    return token;
}

function getRoles() {
    return JSON.parse(localStorage.getItem('roles') || '[]');
}

async function fetchWithAuth(url, options = {}) {
    const token = checkAuth();
    
    if (!options.headers) {
        options.headers = {};
    }
    options.headers['Authorization'] = `Bearer ${token}`;
    options.headers['Content-Type'] = 'application/json';

    const response = await fetch(url, options);

    if (response.status === 401 || response.status === 403) {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('roles');
        localStorage.removeItem('username');
        window.location.href = 'login.html';
        throw new Error('No autorizado');
    }

    return response;
}

document.addEventListener('DOMContentLoaded', () => {
    const token = checkAuth();
    if (!token) return;

    const username = localStorage.getItem('username');
    document.getElementById('user-info').innerText = username;

    const roles = getRoles();
    
    // RBAC: Mostrar panel de registro si es ADMIN u OPERADOR
    if (roles.includes('ROLE_ADMIN') || roles.includes('ROLE_OPERADOR')) {
        document.getElementById('panel-registrar').style.display = 'block';
    } else {
        document.getElementById('panel-registrar').style.display = 'none';
        document.getElementById('menu-reportes').style.display = 'none';
    }

    cargarEnvios();
    
    document.getElementById('form-envio').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const payload = {
            codigoRastreo: document.getElementById('codigoRastreo').value,
            direccionDestino: document.getElementById('direccionDestino').value,
            pesoKg: parseFloat(document.getElementById('pesoKg').value),
            costo: parseFloat(document.getElementById('costo').value),
            vehiculoId: parseInt(document.getElementById('vehiculoId').value),
            conductorId: parseInt(document.getElementById('conductorId').value)
        };

        try {
            const res = await fetchWithAuth(API_URL, {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            
            if (res.ok) {
                alert('Envío registrado con éxito');
                document.getElementById('form-envio').reset();
                cargarEnvios();
            } else {
                const err = await res.json();
                alert('Error al registrar: ' + (JSON.stringify(err)));
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Error de conexión o validación');
        }
    });

    document.getElementById('btn-logout').addEventListener('click', () => {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('roles');
        localStorage.removeItem('username');
        window.location.href = 'login.html';
    });

    document.getElementById('closeBitacoraModal').onclick = function() {
        document.getElementById('bitacoraModal').style.display = "none";
    }

    window.onclick = function(event) {
        const modal = document.getElementById('bitacoraModal');
        if (event.target == modal) {
            modal.style.display = "none";
        }
    }
});

async function cargarEnvios() {
    try {
        const res = await fetchWithAuth(`${API_URL}/optimizados`);
        if (res.ok) {
            enviosGlobal = await res.json();
            renderEnvios(enviosGlobal);
        }
    } catch (error) {
        console.error('Error cargando envíos:', error);
    }
}

function renderEnvios(envios) {
    const grid = document.getElementById('envios-grid');
    grid.innerHTML = '';
    const roles = getRoles();
    
    envios.forEach(envio => {
        const tarjeta = document.createElement('div');
        tarjeta.className = 'tarjeta-envio';
        
        const verBitacoraBtn = (roles.includes('ROLE_ADMIN') || roles.includes('ROLE_OPERADOR')) 
            ? `<button onclick="verBitacora(${envio.id})">Ver Bitácora</button>` : '';

        tarjeta.innerHTML = `
            <h4>${envio.codigoRastreo}</h4>
            <span class="pill-status status-${envio.estadoEnvio}">${envio.estadoEnvio.replace('_', ' ')}</span>
            <p><strong>Destino:</strong> ${envio.direccionDestino}</p>
            <p><strong>Peso:</strong> ${envio.pesoKg} kg</p>
            <p><strong>Costo:</strong> ₡${envio.costo}</p>
            <p><strong>Vehículo:</strong> ${envio.placaVehiculo || 'N/A'}</p>
            <p><strong>Conductor:</strong> ${envio.nombreConductor || 'N/A'}</p>
            <div class="acciones">
                ${envio.estadoEnvio === 'PENDIENTE' ? `<button onclick="actualizarEstado(${envio.id}, 'EN_TRANSITO')">Marcar en Tránsito</button>` : ''}
                ${envio.estadoEnvio === 'EN_TRANSITO' ? `<button onclick="actualizarEstado(${envio.id}, 'ENTREGADO')">Marcar Entregado</button>` : ''}
                ${verBitacoraBtn}
            </div>
        `;
        
        grid.appendChild(tarjeta);
    });
}

async function actualizarEstado(id, nuevoEstado) {
    const obs = prompt("Ingrese justificación para el cambio de estado:");
    if (obs === null) return; // Cancelado

    try {
        const res = await fetchWithAuth(`${API_URL}/${id}/estado`, {
            method: 'PATCH',
            body: JSON.stringify({ nuevoEstado: nuevoEstado, observaciones: obs })
        });
        
        if (res.ok) {
            cargarEnvios();
        } else {
            const err = await res.json();
            alert('Error actualizando estado: ' + (err.error || JSON.stringify(err)));
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

function filtrar(estado) {
    if (estado === 'TODOS') {
        renderEnvios(enviosGlobal);
    } else {
        const filtrados = enviosGlobal.filter(e => e.estadoEnvio === estado);
        renderEnvios(filtrados);
    }
}

async function verBitacora(envioId) {
    try {
        const res = await fetchWithAuth(`${API_URL}/${envioId}/bitacora`);
        if (res.ok) {
            bitacoraActual = await res.json();
            renderBitacora(bitacoraActual);
            document.getElementById('bitacoraModal').style.display = "block";
        }
    } catch (error) {
        console.error('Error cargando bitácora:', error);
    }
}

function renderBitacora(registros) {
    const list = document.getElementById('bitacora-list');
    list.innerHTML = '';
    if (registros.length === 0) {
        list.innerHTML = '<p>No hay historial para este envío.</p>';
        return;
    }
    
    let html = '<table border="1" width="100%" style="border-collapse: collapse; margin-top: 10px;">';
    html += '<tr><th>Fecha/Hora</th><th>Usuario</th><th>Transición</th><th>Observaciones</th></tr>';
    
    registros.forEach(b => {
        const fechaStr = new Date(b.fechaCambio).toLocaleString();
        html += `<tr>
            <td>${fechaStr}</td>
            <td>${b.usuario}</td>
            <td>${b.estadoAnterior} &rarr; ${b.estadoNuevo}</td>
            <td>${b.observaciones || ''}</td>
        </tr>`;
    });
    html += '</table>';
    list.innerHTML = html;
}

window.filtrarBitacora = function() {
    const inicioStr = document.getElementById('filtroFechaInicio').value;
    const finStr = document.getElementById('filtroFechaFin').value;
    
    let filtrados = bitacoraActual;
    
    if (inicioStr) {
        const inicioDate = new Date(inicioStr + 'T00:00:00');
        filtrados = filtrados.filter(b => new Date(b.fechaCambio) >= inicioDate);
    }
    if (finStr) {
        const finDate = new Date(finStr + 'T23:59:59');
        filtrados = filtrados.filter(b => new Date(b.fechaCambio) <= finDate);
    }
    
    renderBitacora(filtrados);
}
