import { useCallback, useEffect, useState } from 'react';
import { biohubApi } from '../api/biohubApi';
import { Navbar } from '../components/Navbar';

export function DirectorPage() {
  const [hallazgos, setHallazgos] = useState([]);
  const [hallazgoId, setHallazgoId] = useState(1);
  const [nombreCientifico, setNombreCientifico] = useState('');
  const [nombreComun, setNombreComun] = useState('');
  const [estadoConservacion, setEstadoConservacion] = useState('EN');
  const [esSensible, setEsSensible] = useState(true);
  const [motivo, setMotivo] = useState('');
  const [fecha, setFecha] = useState('2026-12-10T14:00:00');
  const [mensaje, setMensaje] = useState(null);

  const cargar = useCallback(() => {
    biohubApi.listarHallazgos().then((lista) => {
      setHallazgos(lista);
      setHallazgoId((actual) =>
        lista.length > 0 && !lista.some((x) => x.id === actual) ? lista[0].id : actual
      );
    });
  }, []);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const handleReclasificar = async (e) => {
    e.preventDefault();
    setMensaje(null);
    try {
      await biohubApi.reclasificar(hallazgoId, {
        nuevaEspecieId: Date.now(),
        nombreCientifico,
        nombreComun,
        estadoConservacion,
        esSensible,
        motivo,
        fechaReclasificacion: fecha,
      });
      setMensaje({ tipo: 'ok', texto: `Hallazgo #${hallazgoId} reclasificado y snapshot Memento guardado.` });
      setMotivo('');
      cargar();
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    }
  };

  return (
    <>
      <Navbar titulo="Dirección Científica · Reclasificación Taxonómica" />
      <main className="container two-cols">
        <div className="card">
          <h2>Autorizar Reclasificación (HU-05)</h2>
          <form onSubmit={handleReclasificar}>
            <label>Hallazgo a Reclasificar</label>
            <select value={hallazgoId} onChange={(e) => setHallazgoId(Number(e.target.value))}>
              {hallazgos.map((h) => (
                <option key={h.id} value={h.id}>
                  #{h.id} · {h.nombreCientifico} ({h.departamento})
                </option>
              ))}
            </select>

            <label>Nuevo Nombre Científico</label>
            <input value={nombreCientifico} onChange={(e) => setNombreCientifico(e.target.value)} required />

            <label>Nuevo Nombre Común</label>
            <input value={nombreComun} onChange={(e) => setNombreComun(e.target.value)} required />

            <label>Estado de Conservación</label>
            <select value={estadoConservacion} onChange={(e) => setEstadoConservacion(e.target.value)}>
              <option value="CR">CR</option>
              <option value="EN">EN</option>
              <option value="VU">VU</option>
              <option value="LC">LC</option>
            </select>

            <label>¿Especie sensible?</label>
            <select value={String(esSensible)} onChange={(e) => setEsSensible(e.target.value === 'true')}>
              <option value="true">Sí (Ocultar coordenadas al público)</option>
              <option value="false">No (Coordenadas públicas)</option>
            </select>

            <label>Motivo Científico</label>
            <input value={motivo} onChange={(e) => setMotivo(e.target.value)} required />

            <label>Fecha de Reclasificación (ISO)</label>
            <input value={fecha} onChange={(e) => setFecha(e.target.value)} required />

            <button type="submit" className="btn-primary full">Autorizar Reclasificación</button>
          </form>
          {mensaje && <div className={`alert ${mensaje.tipo}`}>{mensaje.texto}</div>}
        </div>

        <div className="card">
          <h2>Inventario Taxonómico Actual</h2>
          {hallazgos.map((h) => (
            <div key={h.id} className="item-box">
              <span className="pill">Hallazgo #{h.id} · {h.estadoConservacion}</span>
              <h3>{h.nombreCientifico}</h3>
              <p>{h.nombreComun} · {h.ubicacionVisible}</p>
            </div>
          ))}
        </div>
      </main>
    </>
  );
}