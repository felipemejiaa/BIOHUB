import { useEffect, useMemo, useState } from 'react';
import { biohubApi } from '../api/biohubApi';
import { FichaHallazgo, LineaTiempo } from '../components/Hallazgos';
import { Pagina } from '../components/Navbar';
import {
  Aviso,
  Cargando,
  Cifra,
  DistribucionUICN,
  EtiquetaEstado,
  EtiquetaGrupo,
  Icono,
  Modal,
  NombreCientifico,
  Vacio,
} from '../components/ui';
import { useToast } from '../context/ToastContext';
import { useHallazgos } from '../hooks/useHallazgos';
import {
  aLocalDateTime,
  ahoraLocal,
  ESTADOS_ASIGNABLES,
  formatearFecha,
  infoEstado,
  inferirGrupo,
  normalizar,
} from '../utils/taxonomia';

export function DirectorPage() {
  const { hallazgos, cargando, error, recargar } = useHallazgos();
  const [busqueda, setBusqueda] = useState('');
  const [aReclasificar, setAReclasificar] = useState(null);
  const [historialDe, setHistorialDe] = useState(null);
  const [fichaDe, setFichaDe] = useState(null);

  const filtrados = useMemo(() => {
    const q = normalizar(busqueda.trim());
    return hallazgos.filter(
      (h) => !q || normalizar(`${h.id} ${h.nombreCientifico} ${h.nombreComun} ${h.departamento}`).includes(q)
    );
  }, [hallazgos, busqueda]);

  const porDepartamento = useMemo(() => {
    const conteo = {};
    hallazgos.forEach((h) => {
      conteo[h.departamento] = (conteo[h.departamento] || 0) + 1;
    });
    return Object.entries(conteo).sort((a, b) => b[1] - a[1]);
  }, [hallazgos]);

  const sensibles = hallazgos.filter((h) => h.esSensible).length;
  const amenazadas = hallazgos.filter((h) => ['CR', 'EN', 'VU'].includes(h.estadoConservacion)).length;
  const maxDepto = porDepartamento[0]?.[1] || 1;

  return (
    <Pagina
      titulo="Dirección científica"
      descripcion="Inventario completo con coordenadas exactas. Desde aquí autorizas reclasificaciones y revisas el historial de cada hallazgo."
    >
      <section className="resumen">
        <div className="panel resumen-cifras">
          <Cifra valor={hallazgos.length} etiqueta="hallazgos" />
          <Cifra valor={amenazadas} etiqueta="amenazados" detalle="CR, EN o VU" />
          <Cifra valor={sensibles} etiqueta="con ubicación protegida" />
          <Cifra valor={porDepartamento.length} etiqueta="departamentos" />
        </div>
        <div className="panel">
          <h2 className="panel-titulo">Estado de conservación</h2>
          <DistribucionUICN hallazgos={hallazgos} />
        </div>
        <div className="panel">
          <h2 className="panel-titulo">Hallazgos por departamento</h2>
          <ul className="barras">
            {porDepartamento.map(([depto, n]) => (
              <li key={depto}>
                <span className="barras-nombre">{depto}</span>
                <span className="barras-pista">
                  <span style={{ width: `${(n / maxDepto) * 100}%` }} />
                </span>
                <strong>{n}</strong>
              </li>
            ))}
          </ul>
        </div>
      </section>

      <section className="panel">
        <div className="panel-cabecera">
          <h2>Inventario taxonómico</h2>
          <label className="buscador buscador-pequeno">
            <Icono nombre="lupa" tamano={16} />
            <span className="sr-only">Buscar en el inventario</span>
            <input
              type="search"
              placeholder="Buscar por especie, número o departamento"
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
            />
          </label>
        </div>

        {error && <Aviso>{error}</Aviso>}

        {cargando ? (
          <Cargando filas={4} />
        ) : filtrados.length === 0 ? (
          <Vacio titulo="Ningún hallazgo coincide">Prueba con otro término de búsqueda.</Vacio>
        ) : (
          <div className="tabla-contenedor">
            <table className="tabla">
              <thead>
                <tr>
                  <th scope="col">N.º</th>
                  <th scope="col">Especie</th>
                  <th scope="col">Estado</th>
                  <th scope="col">Coordenadas exactas</th>
                  <th scope="col">Observado</th>
                  <th scope="col">
                    <span className="sr-only">Acciones</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {filtrados.map((h) => (
                  <tr key={h.id}>
                    <td className="numero">{h.id}</td>
                    <td>
                      <button className="enlace-especie" onClick={() => setFichaDe(h)}>
                        <NombreCientifico>{h.nombreCientifico}</NombreCientifico>
                        <span className="texto-suave">{h.nombreComun}</span>
                      </button>
                      <EtiquetaGrupo grupo={inferirGrupo(h.atributos)} />
                    </td>
                    <td>
                      <EtiquetaEstado codigo={h.estadoConservacion} />
                      {h.esSensible && (
                        <span className="icono-sensible" title="Especie sensible">
                          <Icono nombre="candado" tamano={14} />
                          <span className="sr-only">Especie sensible</span>
                        </span>
                      )}
                    </td>
                    <td className="coordenadas">{h.ubicacionVisible}</td>
                    <td className="numero">{formatearFecha(h.fechaObservacion)}</td>
                    <td className="acciones">
                      <button className="btn btn-pequeno btn-secundario" onClick={() => setHistorialDe(h)}>
                        <Icono nombre="historial" tamano={15} />
                        Historial
                      </button>
                      <button className="btn btn-pequeno btn-primario" onClick={() => setAReclasificar(h)}>
                        <Icono nombre="editar" tamano={15} />
                        Reclasificar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <Modal
        abierto={Boolean(aReclasificar)}
        onCerrar={() => setAReclasificar(null)}
        titulo={aReclasificar ? `Reclasificar el hallazgo ${aReclasificar.id}` : ''}
      >
        {aReclasificar && (
          <FormularioReclasificacion
            key={aReclasificar.id}
            hallazgo={aReclasificar}
            onListo={() => {
              setAReclasificar(null);
              recargar();
            }}
          />
        )}
      </Modal>

      <Modal
        abierto={Boolean(historialDe)}
        onCerrar={() => setHistorialDe(null)}
        titulo={historialDe ? `Historial del hallazgo ${historialDe.id}` : ''}
      >
        {historialDe && <HistorialHallazgo key={historialDe.id} id={historialDe.id} />}
      </Modal>

      <Modal abierto={Boolean(fichaDe)} onCerrar={() => setFichaDe(null)} titulo="Ficha del hallazgo">
        {fichaDe && <FichaHallazgo hallazgo={fichaDe} />}
      </Modal>
    </Pagina>
  );
}

function FormularioReclasificacion({ hallazgo, onListo }) {
  const notificar = useToast();
  const [form, setForm] = useState({
    nombreCientifico: hallazgo.nombreCientifico,
    nombreComun: hallazgo.nombreComun,
    estadoConservacion: hallazgo.estadoConservacion,
    esSensible: hallazgo.esSensible,
    motivo: '',
    fecha: ahoraLocal(),
  });
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  const actualizar = (campo) => (e) => {
    const valor = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setForm((f) => ({ ...f, [campo]: valor }));
  };

  const sinCambios =
    form.nombreCientifico.trim() === hallazgo.nombreCientifico &&
    form.nombreComun.trim() === hallazgo.nombreComun &&
    form.estadoConservacion === hallazgo.estadoConservacion &&
    form.esSensible === hallazgo.esSensible;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setEnviando(true);
    try {
      await biohubApi.reclasificar(hallazgo.id, {
        nuevaEspecieId: Date.now(),
        nombreCientifico: form.nombreCientifico.trim(),
        nombreComun: form.nombreComun.trim(),
        estadoConservacion: form.estadoConservacion,
        esSensible: form.esSensible,
        motivo: form.motivo.trim(),
        fechaReclasificacion: aLocalDateTime(form.fecha),
      });
      notificar(`Hallazgo ${hallazgo.id} reclasificado. La versión anterior quedó en el historial.`);
      onListo();
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <form className="formulario" onSubmit={handleSubmit}>
      <div className="clasificacion-actual">
        <span className="texto-suave">Clasificación actual</span>
        <NombreCientifico como="p">{hallazgo.nombreCientifico}</NombreCientifico>
        <span className="texto-suave">
          {hallazgo.nombreComun}, {infoEstado(hallazgo.estadoConservacion).nombre.toLowerCase()}
        </span>
      </div>

      <div className="fila-2">
        <div className="campo">
          <label htmlFor="r-cientifico">Nuevo nombre científico</label>
          <input
            id="r-cientifico"
            className="input-binomial"
            value={form.nombreCientifico}
            onChange={actualizar('nombreCientifico')}
            required
          />
        </div>
        <div className="campo">
          <label htmlFor="r-comun">Nuevo nombre común</label>
          <input id="r-comun" value={form.nombreComun} onChange={actualizar('nombreComun')} required />
        </div>
      </div>

      <div className="fila-2">
        <div className="campo">
          <label htmlFor="r-estado">Estado de conservación</label>
          <select id="r-estado" value={form.estadoConservacion} onChange={actualizar('estadoConservacion')}>
            {ESTADOS_ASIGNABLES.map((c) => (
              <option key={c} value={c}>
                {c}: {infoEstado(c).nombre}
              </option>
            ))}
          </select>
        </div>
        <div className="campo">
          <label htmlFor="r-fecha">Fecha de la reclasificación</label>
          <input id="r-fecha" type="datetime-local" value={form.fecha} onChange={actualizar('fecha')} required />
        </div>
      </div>

      <label className="interruptor">
        <input type="checkbox" checked={form.esSensible} onChange={actualizar('esSensible')} />
        <span className="interruptor-pista" aria-hidden="true" />
        <span>
          <strong>Especie sensible</strong>
          <small>Oculta las coordenadas exactas al resto de roles.</small>
        </span>
      </label>

      <div className="campo">
        <label htmlFor="r-motivo">Motivo científico</label>
        <textarea
          id="r-motivo"
          rows={3}
          value={form.motivo}
          onChange={actualizar('motivo')}
          placeholder="Ej.: el análisis genético de la muestra ADN-VG-2026-88 corresponde a otra especie."
          required
        />
        <small className="ayuda">Queda registrado en el historial junto con tu usuario. No se puede editar después.</small>
      </div>

      {error && <Aviso>{error}</Aviso>}

      <div className="formulario-acciones">
        <button type="submit" className="btn btn-primario" disabled={enviando || sinCambios}>
          {enviando ? 'Autorizando…' : 'Autorizar reclasificación'}
        </button>
      </div>
      {sinCambios && <p className="ayuda alinear-derecha">Cambia al menos un dato de la especie para continuar.</p>}
    </form>
  );
}

function HistorialHallazgo({ id }) {
  const [versiones, setVersiones] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let activo = true;
    biohubApi
      .obtenerHistorial(id)
      .then((data) => activo && setVersiones(data))
      .catch((err) => activo && setError(err.message));
    return () => {
      activo = false;
    };
  }, [id]);

  if (error) return <Aviso>{error}</Aviso>;
  if (!versiones) return <Cargando />;
  return <LineaTiempo versiones={versiones} />;
}
