import { Link, NavLink, useNavigate } from 'react-router-dom';
import { rutaPorRol, useAuth } from '../context/AuthContext';
import { nombreRol } from '../utils/taxonomia';
import { Icono, Logo } from './ui';

const TITULO_PANEL = {
  DIRECTOR_CIENTIFICO: 'Dirección científica',
  INVESTIGADOR_CAMPO: 'Registro de campo',
  AUDITOR_CIENTIFICO: 'Auditoría',
};

export function Navbar() {
  const { sesion, cerrarSesion } = useAuth();
  const navigate = useNavigate();

  const handleSalir = async () => {
    await cerrarSesion();
    navigate('/');
  };

  const panel = sesion && TITULO_PANEL[sesion.rol];

  return (
    <header className="barra">
      <div className="barra-interior">
        <Link to={sesion ? rutaPorRol(sesion.rol) : '/'} className="barra-marca" aria-label="Inicio de BioHub">
          <Logo claro />
        </Link>

        <nav className="barra-nav" aria-label="Secciones">
          {panel && <NavLink to={rutaPorRol(sesion.rol)}>{panel}</NavLink>}
          <NavLink to="/publico">Catálogo público</NavLink>
        </nav>

        <div className="barra-usuario">
          {sesion ? (
            <>
              <span className="barra-persona">
                <span className="avatar" aria-hidden="true">
                  {sesion.username.slice(0, 1).toUpperCase()}
                </span>
                <span>
                  <strong>{sesion.username}</strong>
                  <small>{nombreRol(sesion.rol)}</small>
                </span>
              </span>
              <button className="btn-barra" onClick={handleSalir}>
                <Icono nombre="salir" tamano={16} />
                Cerrar sesión
              </button>
            </>
          ) : (
            <button className="btn-barra" onClick={() => navigate('/')}>
              <Icono nombre="entrar" tamano={16} />
              Iniciar sesión
            </button>
          )}
        </div>
      </div>
    </header>
  );
}

export function Pagina({ titulo, descripcion, acciones, children }) {
  return (
    <>
      <Navbar />
      <main className="pagina">
        <div className="pagina-cabecera">
          <div>
            <h1>{titulo}</h1>
            {descripcion && <p className="pagina-descripcion">{descripcion}</p>}
          </div>
          {acciones && <div className="pagina-acciones">{acciones}</div>}
        </div>
        {children}
      </main>
    </>
  );
}
