import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { iniciarSesion, rutaPorRol } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const sesion = await iniciarSesion(username, password);
      navigate(rutaPorRol(sesion.rol));
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="card auth-card">
        <h1>BioHub · Biodiversidad</h1>
        <p className="subtitle">Plataforma Científica de Monitoreo Taxonómico</p>

        <form onSubmit={handleSubmit}>
          <label>Usuario</label>
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="director / investigador / auditor / externo"
            required
          />

          <label>Contraseña</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            required
          />

          <button type="submit" className="btn-primary full">
            Ingresar al Sistema
          </button>
        </form>

        {error && <div className="alert error">{error}</div>}

        <div className="auth-footer">
          <Link to="/registro">¿No tienes cuenta? Regístrate aquí</Link>
          <button className="btn-outline full" onClick={() => navigate('/publico')}>
            Explorar como Visitante Público
          </button>
        </div>
      </div>
    </div>
  );
}