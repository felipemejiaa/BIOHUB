/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useEffect, useState } from 'react';
import { biohubApi, guardarToken, obtenerTokenGuardado } from '../api/biohubApi';

const AuthContext = createContext(null);

export const rutaPorRol = (rol) => {
  switch (rol) {
    case 'DIRECTOR_CIENTIFICO':
      return '/director';
    case 'INVESTIGADOR_CAMPO':
      return '/investigador';
    case 'AUDITOR_CIENTIFICO':
      return '/auditor';
    default:
      return '/publico';
  }
};

export function AuthProvider({ children }) {
  const [sesion, setSesion] = useState(null);
  const [cargando, setCargando] = useState(() => Boolean(obtenerTokenGuardado()));

  useEffect(() => {
    const token = obtenerTokenGuardado();
    if (!token) return;

    biohubApi
      .obtenerSesion()
      .then((s) => setSesion(s))
      .catch(() => guardarToken(''))
      .finally(() => setCargando(false));
  }, []);

  const iniciarSesion = async (username, password) => {
    const s = await biohubApi.login(username, password);
    guardarToken(s.token);
    setSesion(s);
    return s;
  };

  const registrar = async (username, password, rol) => {
    const s = await biohubApi.registro(username, password, rol);
    guardarToken(s.token);
    setSesion(s);
    return s;
  };

  const cerrarSesion = async () => {
    try {
      await biohubApi.logout();
    } finally {
      guardarToken('');
      setSesion(null);
    }
  };

  return (
    <AuthContext.Provider
      value={{ sesion, cargando, iniciarSesion, registrar, cerrarSesion, rutaPorRol }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);