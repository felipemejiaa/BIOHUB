import { useEffect, useMemo, useState } from 'react';
import { biohubApi } from '../api/biohubApi';
import { LineaTiempo } from '../components/Hallazgos';
import { Pagina } from '../components/Navbar';
import {
  Aviso,
  Cargando,
  EscalaUICN,
  EtiquetaEstado,
  Icono,
  NombreCientifico,
  Pestanas,
  Vacio,
} from '../components/ui';
import { useHallazgos } from '../hooks/useHallazgos';
import { aLocalDateTime, ahoraLocal, formatearFechaHora, normalizar } from '../utils/taxonomia';

export function AuditorPage() {
  const { hallazgos, cargando, error } = useHallazgos();
  const [elegido, setElegido] = useState(null);
  const [busqueda, setBusqueda] = useState('');

  const seleccionadoId = elegido ?? hallazgos[0]?.id ?? null;
  const seleccionado = hallazgos.find((h) => h.id === seleccionadoId);

  const filtrados = useMemo(() => {
    const q = normalizar(busqueda.trim());
    return hallazgos.filter(
      (h) => !q || normalizar(`${h.id} ${h.nombreCientifico} ${h.nombreComun}`).includes(q)
    );
  }, [hallazgos, busqueda]);

  return (
    <Pagina
      titulo="Auditoría científica"
      descripcion="Cada reclasificación crea una versión nueva sin borrar la anterior. Revisa quién autorizó cada cambio y compara lo que se sabía en dos fechas."
    >
      <div className="auditoria">
        <aside className="panel panel-lista">
          <div className="panel-cabecera">
            <h2>Hallazgos</h2>
            <span className="contador">{hallazgos.length}</span>
          </div>
          <label className="buscador buscador-pequeno">
            <Icono nombre="lupa" tamano={16} />
            <span className="sr-only">Buscar hallazgo</span>
            <input
              type="search"
              placeholder="Buscar"
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
            />
          </label>
          {error && <Aviso>{error}</Aviso>}
          {cargando ? (
            <Cargando />
          ) : (
            <ul className="lista-compacta">
              {filtrados.map((h) => (
                <li key={h.id}>
                  <button
                    className={h.id === seleccionadoId ? 'seleccionado' : ''}
                    aria-current={h.id === seleccionadoId ? 'true' : undefined}
                    onClick={() => setElegido(h.id)}
                  >
                    <span className="lista-compacta-principal">
                      <NombreCientifico>{h.nombreCientifico}</NombreCientifico>
                      <span className="texto-suave">Hallazgo {h.id}</span>
                    </span>
                    <EtiquetaEstado codigo={h.estadoConservacion} />
                  </button>
                </li>
              ))}
            </ul>
          )}
        </aside>

        <section className="panel">
          {seleccionado ? (
            <DetalleAuditoria key={seleccionado.id} hallazgo={seleccionado} />
          ) : (
            !cargando && <Vacio titulo="No hay hallazgos para auditar" />
          )}
        </section>
      </div>
    </Pagina>
  );
}

function DetalleAuditoria({ hallazgo }) {
  const [vista, setVista] = useState('linea');
  const [versiones, setVersiones] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let activo = true;
    biohubApi
      .obtenerHistorial(hallazgo.id)
      .then((data) => activo && setVersiones(data))
      .catch((err) => activo && setError(err.message));
    return () => {
      activo = false;
    };
  }, [hallazgo.id]);

  return (
    <>
      <div className="auditoria-cabecera">
        <div>
          <span className="texto-suave">Hallazgo {hallazgo.id}, {hallazgo.departamento}</span>
          <NombreCientifico como="h2" className="auditoria-binomial">
            {hallazgo.nombreCientifico}
          </NombreCientifico>
          <p className="texto-suave">{hallazgo.nombreComun}</p>
        </div>
        <EscalaUICN codigo={hallazgo.estadoConservacion} />
      </div>

      <Pestanas
        valor={vista}
        onCambiar={setVista}
        opciones={[
          { valor: 'linea', etiqueta: 'Línea de tiempo', cuenta: versiones?.length },
          { valor: 'comparar', etiqueta: 'Comparar dos fechas' },
        ]}
      />

      {error && <Aviso>{error}</Aviso>}
      {!versiones && !error && <Cargando />}
      {versiones && vista === 'linea' && <LineaTiempo versiones={versiones} />}
      {versiones && vista === 'comparar' && <Comparador hallazgoId={hallazgo.id} versiones={versiones} />}
    </>
  );
}

function Comparador({ hallazgoId, versiones }) {
  const primera = versiones.reduce((a, b) => (a.version < b.version ? a : b));
  const [inicio, setInicio] = useState(primera.fechaVersion.slice(0, 16));
  const [fin, setFin] = useState(ahoraLocal());
  const [resultado, setResultado] = useState(null);
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  const handleComparar = async (e) => {
    e.preventDefault();
    setError('');
    setResultado(null);
    setEnviando(true);
    try {
      setResultado(await biohubApi.comparar(hallazgoId, aLocalDateTime(inicio), aLocalDateTime(fin)));
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="comparador">
      <form className="comparador-form" onSubmit={handleComparar}>
        <div className="campo">
          <label htmlFor="c-inicio">Lo que se sabía el</label>
          <input id="c-inicio" type="datetime-local" value={inicio} onChange={(e) => setInicio(e.target.value)} required />
        </div>
        <div className="campo">
          <label htmlFor="c-fin">Comparado con el</label>
          <input id="c-fin" type="datetime-local" value={fin} onChange={(e) => setFin(e.target.value)} required />
        </div>
        <button type="submit" className="btn btn-primario" disabled={enviando}>
          <Icono nombre="comparar" tamano={16} />
          {enviando ? 'Comparando…' : 'Comparar'}
        </button>
      </form>
      <p className="ayuda">La primera versión de este hallazgo es del {formatearFechaHora(primera.fechaVersion)}.</p>

      {error && <Aviso>{error}</Aviso>}

      {resultado && (
        <div className="comparacion">
          <p className={`veredicto ${resultado.huboCambioDeEspecie ? 'veredicto-cambio' : ''}`}>
            <Icono nombre={resultado.huboCambioDeEspecie ? 'alerta' : 'check'} tamano={18} />
            {resultado.huboCambioDeEspecie
              ? 'La especie cambió entre estas dos fechas.'
              : 'La especie es la misma en las dos fechas.'}
          </p>
          <div className="comparacion-columnas">
            <ColumnaComparacion
              titulo="Antes"
              version={resultado.versionAnterior}
              fecha={resultado.fechaAnterior}
              especie={resultado.especieAnterior}
            />
            <ColumnaComparacion
              titulo="Después"
              version={resultado.versionPosterior}
              fecha={resultado.fechaPosterior}
              especie={resultado.especiePosterior}
            />
          </div>
          {resultado.huboCambioDeEspecie && (
            <dl className="comparacion-autoria">
              <div>
                <dt>Autorizó el cambio</dt>
                <dd>{resultado.autorizadoPorPosterior}</dd>
              </div>
              <div>
                <dt>Motivo</dt>
                <dd>{resultado.motivoCambio}</dd>
              </div>
            </dl>
          )}
        </div>
      )}
    </div>
  );
}

function ColumnaComparacion({ titulo, version, fecha, especie }) {
  return (
    <div className="comparacion-columna">
      <div className="comparacion-columna-cabecera">
        <strong>{titulo}</strong>
        <span className="texto-suave">
          Versión {version}, {formatearFechaHora(fecha)}
        </span>
      </div>
      <NombreCientifico como="p" className="comparacion-binomial">
        {especie.nombreCientifico}
      </NombreCientifico>
      <p className="texto-suave">{especie.nombreComun}</p>
      <EscalaUICN codigo={especie.estadoConservacion} compacta />
    </div>
  );
}
