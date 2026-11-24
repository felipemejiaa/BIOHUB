import { useEffect, useState } from 'react';
import { biohubApi } from '../api/biohubApi';
import { Navbar } from '../components/Navbar';

export function AuditorPage() {
  const [hallazgos, setHallazgos] = useState([]);
  const [hallazgoId, setHallazgoId] = useState(1);
  const [historial, setHistorial] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    biohubApi.listarHallazgos().then(setHallazgos).catch(console.error);
  }, []);

  useEffect(() => {
    let activo = true;
    biohubApi
      .obtenerHistorial(hallazgoId)
      .then((data) => {
        if (activo) {
          setError('');
          setHistorial(data);
        }
      })
      .catch((err) => {
        if (activo) setError(err.message);
      });
    return () => {
      activo = false;
    };
  }, [hallazgoId]);

  return (
    <>
      <Navbar titulo="Auditoría Científica · Historial Memento" />
      <main className="container">
        <div className="card">
          <h2>Línea de Tiempo de Versiones Taxonómicas (HU-05)</h2>
          <label>Selecciona el Hallazgo a Auditar</label>
          <select value={hallazgoId} onChange={(e) => setHallazgoId(Number(e.target.value))}>
            {hallazgos.map((h) => (
              <option key={h.id} value={h.id}>
                Hallazgo #{h.id} · {h.nombreCientifico}
              </option>
            ))}
          </select>

          {error && <div className="alert error">{error}</div>}

          <div className="timeline">
            {historial.map((m) => (
              <div key={m.version} className="item-box">
                <span className="pill">Versión v{m.version} · {m.fechaVersion}</span>
                <h3>{m.estadoHallazgo.especie.nombreCientifico} ({m.estadoHallazgo.especie.nombreComun})</h3>
                <p><strong>Autorizado por:</strong> {m.autorizadoPor} ({m.rolAutorizador})</p>
                <p><strong>Motivo:</strong> {m.motivo}</p>
              </div>
            ))}
          </div>
        </div>
      </main>
    </>
  );
}