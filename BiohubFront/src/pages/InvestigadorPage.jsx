import { useEffect, useState } from 'react';
import { biohubApi } from '../api/biohubApi';
import { Navbar } from '../components/Navbar';

export function InvestigadorPage() {
  const [hallazgos, setHallazgos] = useState([]);
  const [grupo, setGrupo] = useState('ANFIBIO');
  const [requeridos, setRequeridos] = useState({});
  const [valoresAtributos, setValoresAtributos] = useState({});
  const [nombreCientifico, setNombreCientifico] = useState('');
  const [nombreComun, setNombreComun] = useState('');
  const [estadoConservacion, setEstadoConservacion] = useState('EN');
  const [esSensible, setEsSensible] = useState(true);
  const [departamento, setDepartamento] = useState('');
  const [latitud, setLatitud] = useState('');
  const [longitud, setLongitud] = useState('');
  const [mensaje, setMensaje] = useState(null);

  const cargarHallazgos = () => {
    biohubApi.listarHallazgos().then(setHallazgos).catch(console.error);
  };

  useEffect(() => {
    cargarHallazgos();
  }, []);

  useEffect(() => {
    biohubApi.obtenerRequeridos(grupo).then((res) => {
      setRequeridos(res.atributosRequeridos);
      setValoresAtributos({});
    });
  }, [grupo]);

  const handleGuardar = async (e) => {
    e.preventDefault();
    setMensaje(null);
    try {
      const atributos = Object.entries(requeridos).map(([nombre, tipo]) => ({
        nombre,
        tipo,
        valor: valoresAtributos[nombre] || '',
      }));

      await biohubApi.registrarHallazgo({
        grupoTaxonomico: grupo,
        nombreCientifico,
        nombreComun,
        estadoConservacion,
        esSensible,
        latitud: parseFloat(latitud),
        longitud: parseFloat(longitud),
        departamento,
        atributos,
      });

      setMensaje({ tipo: 'ok', texto: 'Hallazgo registrado y persistido correctamente.' });
      setNombreCientifico('');
      setNombreComun('');
      setValoresAtributos({});
      cargarHallazgos();
    } catch (err) {
      setMensaje({ tipo: 'error', texto: err.message });
    }
  };

  return (
    <>
      <Navbar titulo="Estación del Investigador de Campo" />
      <main className="container two-cols">
        <div className="card">
          <h2>Registrar Hallazgo (Factory Method · HU-04)</h2>
          <form onSubmit={handleGuardar}>
            <label>Grupo Taxonómico</label>
            <select value={grupo} onChange={(e) => setGrupo(e.target.value)}>
              <option value="ANFIBIO">ANFIBIO</option>
              <option value="AVE_MIGRATORIA">AVE_MIGRATORIA</option>
              <option value="PLANTA">PLANTA</option>
              <option value="REPTIL">REPTIL</option>
            </select>

            <label>Nombre Científico</label>
            <input value={nombreCientifico} onChange={(e) => setNombreCientifico(e.target.value)} required />

            <label>Nombre Común</label>
            <input value={nombreComun} onChange={(e) => setNombreComun(e.target.value)} required />

            <label>Estado de Conservación</label>
            <select value={estadoConservacion} onChange={(e) => setEstadoConservacion(e.target.value)}>
              <option value="CR">CR (En Peligro Crítico)</option>
              <option value="EN">EN (En Peligro)</option>
              <option value="VU">VU (Vulnerable)</option>
              <option value="LC">LC (Preocupación Menor)</option>
            </select>

            <label>Departamento</label>
            <input value={departamento} onChange={(e) => setDepartamento(e.target.value)} required />

            <div className="row-2">
              <div>
                <label>Latitud</label>
                <input type="number" step="any" value={latitud} onChange={(e) => setLatitud(e.target.value)} required />
              </div>
              <div>
                <label>Longitud</label>
                <input type="number" step="any" value={longitud} onChange={(e) => setLongitud(e.target.value)} required />
              </div>
            </div>

            <label>¿Especie sensible?</label>
            <select value={String(esSensible)} onChange={(e) => setEsSensible(e.target.value === 'true')}>
              <option value="true">Sí (Ocultar coordenadas al público)</option>
              <option value="false">No (Coordenadas públicas)</option>
            </select>

            {Object.entries(requeridos).map(([nombre, tipo]) => (
              <div key={nombre}>
                <label>Atributo obligatorio: {nombre} ({tipo})</label>
                <input
                  value={valoresAtributos[nombre] || ''}
                  onChange={(e) =>
                    setValoresAtributos({ ...valoresAtributos, [nombre]: e.target.value })
                  }
                  required
                />
              </div>
            ))}

            <button type="submit" className="btn-primary full">Guardar Hallazgo</button>
          </form>
          {mensaje && <div className={`alert ${mensaje.tipo}`}>{mensaje.texto}</div>}
        </div>

        <div className="card">
          <h2>Hallazgos Registrados en Campo ({hallazgos.length})</h2>
          {hallazgos.map((h) => (
            <div key={h.id} className="item-box">
              <span className="pill">Hallazgo #{h.id}</span>
              <h3>{h.nombreCientifico} ({h.nombreComun})</h3>
              <p><strong>Ubicación:</strong> {h.ubicacionVisible}</p>
            </div>
          ))}
        </div>
      </main>
    </>
  );
}