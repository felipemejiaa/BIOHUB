import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function RegistroPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rol, setRol] = useState('INVESTIGADOR_CAMPO');
  const [error, setError] = useState('');
  const { registrar, rutaPorRol } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const sesion = await registrar(username, password, rol);
      navigate(rutaPorRol(sesion.rol));
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="card auth-card">
        <h1>Registro de Usuario</h1>
        <p className="subtitle">Crea una cuenta asignando tu rol en BioHub</p>

        <form onSubmit={handleSubmit}>
          <label>Nombre de usuario</label>
          <input value={username} onChange={(e) => setUsername(e.target.value)} required />

          <label>Contraseña</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />

          <label>Rol Institucional</label>
          <select value={rol} onChange={(e) => setRol(e.target.value)}>
            <option value="INVESTIGADOR_CAMPO">Investigador de Campo</option>
            <option value="DIRECTOR_CIENTIFICO">Director Científico</option>
            <option value="AUDITOR_CIENTIFICO">Auditor Científico</option>
            <option value="CONSUMIDOR_EXTERNO">Consumidor Externo (ONG / Universidad)</option>
          </select>

          <button type="submit" className="btn-primary full">
            Crear Cuenta y Entrar
          </button>
        </form>

        {error && <div className="alert error">{error}</div>}

        <div className="auth-footer">
          <Link to="/">Volver al inicio de sesión</Link>
        </div>
      </div>
    </div>
  );
}