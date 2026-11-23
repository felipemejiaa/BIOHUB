import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AuthLayout } from '../components/AuthLayout';
import { Aviso, Icono } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { nombreRol } from '../utils/taxonomia';

// Usuarios que crea BioHubConfiguracionDemo al arrancar el backend.
const CUENTAS_DEMO = [
  { username: 'director', password: 'Director2026*', rol: 'DIRECTOR_CIENTIFICO' },
  { username: 'investigador', password: 'Campo2026*', rol: 'INVESTIGADOR_CAMPO' },
  { username: 'auditor', password: 'Auditor2026*', rol: 'AUDITOR_CIENTIFICO' },
  { username: 'externo', password: 'Externo2026*', rol: 'CONSUMIDOR_EXTERNO' },
];

export function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [verPassword, setVerPassword] = useState(false);
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);
  const { iniciarSesion, rutaPorRol } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setEnviando(true);
    try {
      const sesion = await iniciarSesion(username, password);
      navigate(rutaPorRol(sesion.rol));
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  };

  const usarCuenta = (cuenta) => {
    setUsername(cuenta.username);
    setPassword(cuenta.password);
    setError('');
  };

  return (
    <AuthLayout>
      <div className="auth-formulario">
        <h2>Inicia sesión</h2>
        <p className="texto-suave">Entra con tu usuario institucional para ir a tu panel de trabajo.</p>

        <form onSubmit={handleSubmit} className="formulario">
          <div className="campo">
            <label htmlFor="usuario">Usuario</label>
            <input
              id="usuario"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              required
            />
          </div>

          <div className="campo">
            <label htmlFor="clave">Contraseña</label>
            <div className="campo-con-boton">
              <input
                id="clave"
                type={verPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                required
              />
              <button
                type="button"
                className="btn-icono"
                onClick={() => setVerPassword((v) => !v)}
                aria-label={verPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
              >
                <Icono nombre={verPassword ? 'ojoNo' : 'ojo'} />
              </button>
            </div>
          </div>

          {error && <Aviso>{error}</Aviso>}

          <button type="submit" className="btn btn-primario btn-bloque" disabled={enviando}>
            {enviando ? 'Entrando…' : 'Entrar'}
          </button>
        </form>

        <div className="separador">
          <span>o</span>
        </div>

        <button className="btn btn-secundario btn-bloque" onClick={() => navigate('/publico')}>
          <Icono nombre="lupa" tamano={16} />
          Consultar el catálogo sin cuenta
        </button>

        <p className="auth-pie">
          ¿No tienes cuenta? <Link to="/registro">Crea una</Link>
        </p>

        <details className="demo">
          <summary>Cuentas de demostración</summary>
          <p className="texto-suave">Elige una para llenar el formulario.</p>
          <ul>
            {CUENTAS_DEMO.map((c) => (
              <li key={c.username}>
                <button type="button" onClick={() => usarCuenta(c)}>
                  <strong>{c.username}</strong>
                  <span>{nombreRol(c.rol)}</span>
                </button>
              </li>
            ))}
          </ul>
        </details>
      </div>
    </AuthLayout>
  );
}
