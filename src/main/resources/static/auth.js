const RUTAS_POR_ROL = {
    DIRECTOR_CIENTIFICO: '/director.html',
    AUDITOR_CIENTIFICO: '/auditor.html',
    INVESTIGADOR_CAMPO: '/investigador.html',
    CONSUMIDOR_EXTERNO: '/publico.html',
    VISITANTE_PUBLICO: '/publico.html'
};

function guardarToken(token) {
    if (token) {
        localStorage.setItem('biohub_token', token);
    } else {
        localStorage.removeItem('biohub_token');
    }
}

function obtenerToken() {
    return localStorage.getItem('biohub_token');
}

function headersAuth() {
    const headers = { 'Content-Type': 'application/json' };
    const token = obtenerToken();
    if (token) {
        headers['X-Auth-Token'] = token;
    }
    return headers;
}

function redirigirSegunRol(rol) {
    window.location.href = RUTAS_POR_ROL[rol] || '/publico.html';
}

async function verificarAccesoPagina(rolesPermitidos) {
    const resp = await fetch('/api/auth/sesion', { headers: headersAuth() });
    const sesion = await resp.json();

    if (!rolesPermitidos.includes(sesion.rol)) {
        redirigirSegunRol(sesion.rol);
        return null;
    }

    const badge = document.getElementById('estadoHeader');
    if (badge) {
        badge.textContent = sesion.autenticada
            ? `${sesion.username} · ${sesion.rol}`
            : `VISITANTE_PUBLICO`;
    }
    return sesion;
}

async function cerrarSesionYSalir() {
    await fetch('/api/auth/logout', { method: 'POST', headers: headersAuth() });
    guardarToken(null);
    window.location.href = '/index.html';
}