import { useEffect, useState } from 'react';
import { biohubApi } from '../api/biohubApi';
import { Navbar } from '../components/Navbar';

export function PublicoPage() {
  const [hallazgos, setHallazgos] = useState([]);

  useEffect(() => {
    biohubApi.listarHallazgos().then(setHallazgos).catch(console.error);
  }, []);

  return (
    <>
      <Navbar titulo="Consulta Pública de Biodiversidad" />
      <main className="container">
        <div className="card">
          <h2>Catálogo de Hallazgos Registrados</h2>
          <p className="subtitle">
            Las coordenadas exactas de especies sensibles se protegen automáticamente según tu rol.
          </p>
          <div className="grid-cards">
            {hallazgos.map((h) => (
              <div key={h.id} className="item-box">
                <span className="pill">Hallazgo #{h.id} · {h.estadoConservacion}</span>
                <h3>{h.nombreCientifico}</h3>
                <p>{h.nombreComun}</p>
                <p><strong>Ubicación visible:</strong> {h.ubicacionVisible}</p>
                {h.esSensible && !h.coordenadasExactas && (
                  <span className="tag-warning">Especie sensible · Coordenadas ocultas</span>
                )}
              </div>
            ))}
          </div>
        </div>
      </main>
    </>
  );
}