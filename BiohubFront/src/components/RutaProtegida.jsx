import { Navigate } from 'react-router-dom';
import { rutaPorRol, useAuth } from '../context/AuthContext';

export function RutaProtegida({ rolRequerido, children }) {
  const { sesion, cargando } = useAuth();

  if (cargando) return <div className="container">Cargando sesión...</div>;
  if (!sesion) return <Navigate to="/" replace />;
  if (sesion.rol !== rolRequerido) {
    return <Navigate to={rutaPorRol(sesion.rol)} replace />;
  }
  return children;
}