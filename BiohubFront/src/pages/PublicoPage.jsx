import { useMemo, useState } from 'react';
import { FichaHallazgo, TarjetaHallazgo } from '../components/Hallazgos';
import { Navbar } from '../components/Navbar';
import { Aviso, Cargando, Cifra, Icono, Modal, Vacio } from '../components/ui';
import { useHallazgos } from '../hooks/useHallazgos';
import { GRUPOS, infoEstado, inferirGrupo, normalizar } from '../utils/taxonomia';

export function PublicoPage() {
  const { hallazgos, cargando, error } = useHallazgos();
  const [busqueda, setBusqueda] = useState('');
  const [estado, setEstado] = useState('');
  const [grupo, setGrupo] = useState('');
  const [departamento, setDepartamento] = useState('');
  const [seleccionado, setSeleccionado] = useState(null);

  const departamentos = useMemo(
    () => [...new Set(hallazgos.map((h) => h.departamento))].sort(),
    [hallazgos]
  );
  const estados = useMemo(() => [...new Set(hallazgos.map((h) => h.estadoConservacion))], [hallazgos]);

  const filtrados = useMemo(() => {
    const q = normalizar(busqueda.trim());
    return hallazgos.filter(
      (h) =>
        (!q || normalizar(`${h.nombreCientifico} ${h.nombreComun} ${h.departamento}`).includes(q)) &&
        (!estado || h.estadoConservacion === estado) &&
        (!grupo || inferirGrupo(h.atributos) === grupo) &&
        (!departamento || h.departamento === departamento)
    );
  }, [hallazgos, busqueda, estado, grupo, departamento]);

  const amenazadas = hallazgos.filter((h) => ['CR', 'EN', 'VU'].includes(h.estadoConservacion)).length;
  const hayFiltros = busqueda || estado || grupo || departamento;

  const limpiar = () => {
    setBusqueda('');
    setEstado('');
    setGrupo('');
    setDepartamento('');
  };

  return (
    <>
      <Navbar />
      <section className="portada">
        <div className="portada-interior">
          <h1>Catálogo de biodiversidad</h1>
          <p>Busca entre las especies registradas por los investigadores del Instituto en todo el país.</p>
          <label className="buscador">
            <Icono nombre="lupa" tamano={20} />
            <span className="sr-only">Buscar especies</span>
            <input
              type="search"
              placeholder="Nombre científico, nombre común o departamento"
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
            />
          </label>
          <div className="portada-cifras">
            <Cifra valor={hallazgos.length} etiqueta="hallazgos registrados" />
            <Cifra valor={amenazadas} etiqueta="especies amenazadas" />
            <Cifra valor={departamentos.length} etiqueta="departamentos" />
          </div>
        </div>
      </section>

      <main className="pagina">
        <div className="filtros">
          <div className="filtro-chips" role="group" aria-label="Filtrar por estado de conservación">
            <button className={!estado ? 'chip activo' : 'chip'} onClick={() => setEstado('')}>
              Todos los estados
            </button>
            {estados.map((codigo) => (
              <button
                key={codigo}
                className={estado === codigo ? 'chip activo' : 'chip'}
                onClick={() => setEstado(estado === codigo ? '' : codigo)}
              >
                <span className="punto" style={{ background: infoEstado(codigo).color }} />
                {infoEstado(codigo).nombre}
              </button>
            ))}
          </div>
          <div className="filtro-selects">
            <select value={grupo} onChange={(e) => setGrupo(e.target.value)} aria-label="Grupo taxonómico">
              <option value="">Todos los grupos</option>
              {Object.entries(GRUPOS).map(([clave, g]) => (
                <option key={clave} value={clave}>
                  {g.nombre}
                </option>
              ))}
            </select>
            <select
              value={departamento}
              onChange={(e) => setDepartamento(e.target.value)}
              aria-label="Departamento"
            >
              <option value="">Todos los departamentos</option>
              {departamentos.map((d) => (
                <option key={d} value={d}>
                  {d}
                </option>
              ))}
            </select>
          </div>
        </div>

        {error && <Aviso>No se pudo cargar el catálogo: {error}</Aviso>}

        {cargando ? (
          <div className="rejilla">
            <Cargando filas={4} />
            <Cargando filas={4} />
            <Cargando filas={4} />
          </div>
        ) : filtrados.length === 0 ? (
          <Vacio
            titulo={hayFiltros ? 'Ninguna especie coincide con tu búsqueda' : 'Aún no hay hallazgos publicados'}
            accion={
              hayFiltros && (
                <button className="btn btn-secundario" onClick={limpiar}>
                  Quitar filtros
                </button>
              )
            }
          >
            {hayFiltros ? 'Prueba con otro nombre o quita algún filtro.' : null}
          </Vacio>
        ) : (
          <>
            <p className="conteo-resultados">
              {filtrados.length === 1 ? '1 hallazgo' : `${filtrados.length} hallazgos`}
              {hayFiltros && ` de ${hallazgos.length}`}
            </p>
            <div className="rejilla">
              {filtrados.map((h) => (
                <TarjetaHallazgo key={h.id} hallazgo={h} onAbrir={setSeleccionado} />
              ))}
            </div>
          </>
        )}
      </main>

      <Modal abierto={Boolean(seleccionado)} onCerrar={() => setSeleccionado(null)} titulo="Ficha del hallazgo">
        {seleccionado && <FichaHallazgo hallazgo={seleccionado} />}
      </Modal>
    </>
  );
}
