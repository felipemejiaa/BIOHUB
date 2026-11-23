const TOKEN_KEY = 'biohub_token';

export const obtenerTokenGuardado = () => localStorage.getItem(TOKEN_KEY) || '';

export const guardarToken = (token) => {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
};

async function request(url, options = {}) {
  const token = obtenerTokenGuardado();
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };
  if (token) headers['X-Auth-Token'] = token;

  const res = await fetch(url, { ...options, headers });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.error || `Error HTTP ${res.status}`);
  }
  return data;
}

export const biohubApi = {
  login: (username, password) =>
    request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }),

  registro: (username, password, rol) =>
    request('/api/auth/registro', {
      method: 'POST',
      body: JSON.stringify({ username, password, rol }),
    }),

  obtenerSesion: () => request('/api/auth/sesion'),

  logout: () => request('/api/auth/logout', { method: 'POST' }),

  listarHallazgos: () => request('/api/biohub/hallazgos'),

  obtenerRequeridos: (grupo) =>
    request(`/api/biohub/fichas/${grupo}/requeridos`),

  registrarHallazgo: (payload) =>
    request('/api/biohub/hallazgos', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  reclasificar: (id, payload) =>
    request(`/api/biohub/hallazgos/${id}/reclasificar`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  obtenerHistorial: (id) => request(`/api/biohub/hallazgos/${id}/historial`),

  comparar: (id, inicio, fin) =>
    request(
      `/api/biohub/hallazgos/${id}/comparar?inicio=${encodeURIComponent(inicio)}&fin=${encodeURIComponent(fin)}`
    ),
};