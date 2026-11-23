import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/AuthLayout';
import { Aviso } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { ROLES } from '../utils/taxonomia';

const ROLES_REGISTRABLES = ['INVESTIGADOR_CAMPO', 'DIRECTOR_CIENTIFICO', 'AUDITOR_CIENTIFICO', 'CONSUMIDOR_EXTERNO'];

export function RegistroPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmacion, setConfirmacion] = useState('');
  const [rol, setRol] = useState('INVESTIGADOR_CAMPO');
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);
  const { registrar, rutaPorRol } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (password !== confirmacion) {
      setError('Las contraseñas no coinciden. Escríbelas de nuevo.');
      return;
    }
    setEnviando(true);
    try {
      const sesion = await registrar(username.trim(), password, rol);
      navigate(rutaPorRol(sesion.rol));
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <AuthLayout>
      <div className="auth-formulario">
        <h2>Crea tu cuenta</h2>
        <p className="texto-suave">Tu rol define qué puedes ver y hacer dentro de BioHub.</p>

        <form onSubmit={handleSubmit} className="formulario">
          <div className="campo">
            <label htmlFor="nuevo-usuario">Nombre de usuario</label>
            <input
              id="nuevo-usuario"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              required
            />
          </div>

          <div className="fila-2">
            <div className="campo">
              <label htmlFor="nueva-clave">Contraseña</label>
              <input
                id="nueva-clave"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="new-password"
                required
              />
            </div>
            <div className="campo">
              <label htmlFor="confirmar-clave">Confirmar contraseña</label>
              <input
                id="confirmar-clave"
                type="password"
                value={confirmacion}
                onChange={(e) => setConfirmacion(e.target.value)}
                autoComplete="new-password"
                required
              />
            </div>
          </div>

          <fieldset className="campo">
            <legend>Rol institucional</legend>
            <div className="opciones-rol">
              {ROLES_REGISTRABLES.map((clave) => (
                <label key={clave} className={`opcion-rol ${rol === clave ? 'seleccionada' : ''}`}>
                  <input
                    type="radio"
                    name="rol"
                    value={clave}
                    checked={rol === clave}
                    onChange={() => setRol(clave)}
                  />
                  <strong>{ROLES[clave].nombre}</strong>
                  <span>{ROLES[clave].descripcion}</span>
                </label>
              ))}
            </div>
          </fieldset>

          {error && <Aviso>{error}</Aviso>}

          <button type="submit" className="btn btn-primario btn-bloque" disabled={enviando}>
            {enviando ? 'Creando cuenta…' : 'Crear cuenta'}
          </button>
        </form>

        <p className="auth-pie">
          ¿Ya tienes cuenta? <Link to="/">Inicia sesión</Link>
        </p>
      </div>
    </AuthLayout>
  );
}
