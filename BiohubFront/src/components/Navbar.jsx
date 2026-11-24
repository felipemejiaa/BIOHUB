import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function Navbar({ titulo }) {
  const { sesion, cerrarSesion } = useAuth();
  const navigate = useNavigate();

  const handleSalir = async () => {
    await cerrarSesion();
    navigate('/');
  };

  return (
    <header className="navbar">
      <div className="navbar-brand">
        <strong>BioHub</strong> · {titulo}
      </div>
      <div className="navbar-actions">
        <span className="badge-rol">
          {sesion ? `${sesion.username} · ${sesion.rol}` : 'VISITANTE_PUBLICO'}
        </span>
        <button className="btn-accent" onClick={handleSalir}>
          {sesion ? 'Cerrar Sesión' : 'Iniciar Sesión'}
        </button>
      </div>
    </header>
  );
}