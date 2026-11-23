import { useEffect, useMemo, useState } from 'react';
import { biohubApi } from '../api/biohubApi';
import { FichaHallazgo } from '../components/Hallazgos';
import { Pagina } from '../components/Navbar';
import {
  Aviso,
  Cargando,
  EtiquetaEstado,
  EtiquetaGrupo,
  Icono,
  Modal,
  NombreCientifico,
  Vacio,
} from '../components/ui';
import { useToast } from '../context/ToastContext';
import { useHallazgos } from '../hooks/useHallazgos';
import { ESTADOS_ASIGNABLES, GRUPOS, infoAtributo, infoEstado, inferirGrupo, normalizar } from '../utils/taxonomia';

const DEPARTAMENTOS = [
  'Amazonas', 'Antioquia', 'Arauca', 'Atlántico', 'Bogotá D.C.', 'Bolívar', 'Boyacá', 'Caldas', 'Caquetá',
  'Casanare', 'Cauca', 'Cesar', 'Chocó', 'Córdoba', 'Cundinamarca', 'Guainía', 'Guaviare', 'Huila',
  'La Guajira', 'Magdalena', 'Meta', 'Nariño', 'Norte de Santander', 'Putumayo', 'Quindío', 'Risaralda',
  'San Andrés y Providencia', 'Santander', 'Sucre', 'Tolima', 'Valle del Cauca', 'Vaupés', 'Vichada',
];

const FORMULARIO_VACIO = {
  nombreCientifico: '',
  nombreComun: '',
  estadoConservacion: 'EN',
  esSensible: true,
  departamento: '',
  latitud: '',
  longitud: '',
};

export function InvestigadorPage() {
  const { hallazgos, cargando, recargar } = useHallazgos();
  const notificar = useToast();
  const [grupo, setGrupo] = useState('ANFIBIO');
  const [requeridos, setRequeridos] = useState([]);
  const [valores, setValores] = useState({});
  const [form, setForm] = useState(FORMULARIO_VACIO);
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [busqueda, setBusqueda] = useState('');
  const [seleccionado, setSeleccionado] = useState(null);

  // El backend responde con la lista de atributos obligatorios de la ficha (Factory Method).
  useEffect(() => {
    let activo = true;
    biohubApi
      .obtenerRequeridos(grupo)
      .then((res) => {
        if (!activo) return;
        const lista = res.atributosRequeridos;
        setRequeridos(Array.isArray(lista) ? lista : Object.keys(lista || {}));
        setValores({});
      })
      .catch(() => activo && setRequeridos(Object.keys(GRUPOS[grupo].atributos)));
    return () => {
      activo = false;
    };
  }, [grupo]);

  const actualizar = (campo) => (e) => {
    const valor = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setForm((f) => ({ ...f, [campo]: valor }));
  };

  const handleGuardar = async (e) => {
    e.preventDefault();
    setError('');
    setEnviando(true);
    try {
      const atributos = requeridos.map((nombre) => ({
        nombre,
        tipo: infoAtributo(nombre).tipo,
        valor: (valores[nombre] || '').trim(),
      }));

      const creado = await biohubApi.registrarHallazgo({
        grupoTaxonomico: grupo,
        nombreCientifico: form.nombreCientifico.trim(),
        nombreComun: form.nombreComun.trim(),
        estadoConservacion: form.estadoConservacion,
        esSensible: form.esSensible,
        latitud: parseFloat(form.latitud),
        longitud: parseFloat(form.longitud),
        departamento: form.departamento.trim(),
        atributos,
      });

      notificar(
        creado?.id ? `Hallazgo ${creado.id} registrado: ${form.nombreCientifico}` : 'Hallazgo registrado'
      );
      setForm(FORMULARIO_VACIO);
      setValores({});
      recargar();
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  };

  const recientes = useMemo(() => {
    const q = normalizar(busqueda.trim());
    return [...hallazgos]
      .sort((a, b) => (b.fechaObservacion || '').localeCompare(a.fechaObservacion || ''))
      .filter((h) => !q || normalizar(`${h.nombreCientifico} ${h.nombreComun} ${h.departamento}`).includes(q));
  }, [hallazgos, busqueda]);

  return (
    <Pagina
      titulo="Registro de campo"
      descripcion="Registra cada observación con la ficha de su grupo taxonómico. Los campos de la ficha cambian según el grupo."
    >
      <div className="dos-columnas">
        <form className="panel formulario" onSubmit={handleGuardar}>
          <section className="paso">
            <h2 className="paso-titulo">
              <span className="paso-numero">1</span>
              Grupo taxonómico
            </h2>
            <div className="opciones-grupo" role="radiogroup" aria-label="Grupo taxonómico">
              {Object.entries(GRUPOS).map(([clave, g]) => (
                <label key={clave} className={`opcion-grupo ${grupo === clave ? 'seleccionada' : ''}`}>
                  <input
                    type="radio"
                    name="grupo"
                    value={clave}
                    checked={grupo === clave}
                    onChange={() => setGrupo(clave)}
                  />
                  <strong>{g.nombre}</strong>
                  <span>{g.descripcion}</span>
                </label>
              ))}
            </div>
          </section>

          <section className="paso">
            <h2 className="paso-titulo">
              <span className="paso-numero">2</span>
              Especie
            </h2>
            <div className="fila-2">
              <div className="campo">
                <label htmlFor="nombre-cientifico">Nombre científico</label>
                <input
                  id="nombre-cientifico"
                  className="input-binomial"
                  value={form.nombreCientifico}
                  onChange={actualizar('nombreCientifico')}
                  placeholder="Dendrobates truncatus"
                  required
                />
              </div>
              <div className="campo">
                <label htmlFor="nombre-comun">Nombre común</label>
                <input
                  id="nombre-comun"
                  value={form.nombreComun}
                  onChange={actualizar('nombreComun')}
                  placeholder="Rana venenosa"
                  required
                />
              </div>
            </div>
            <div className="campo">
              <span className="etiqueta">Estado de conservación</span>
              <div className="opciones-estado" role="radiogroup" aria-label="Estado de conservación">
                {ESTADOS_ASIGNABLES.map((codigo) => {
                  const info = infoEstado(codigo);
                  const activo = form.estadoConservacion === codigo;
                  return (
                    <label
                      key={codigo}
                      className={`opcion-estado ${activo ? 'seleccionada' : ''}`}
                      style={activo ? { background: info.color, color: info.texto, borderColor: info.color } : undefined}
                    >
                      <input
                        type="radio"
                        name="estado"
                        value={codigo}
                        checked={activo}
                        onChange={actualizar('estadoConservacion')}
                      />
                      <strong>{codigo}</strong>
                      <span>{info.nombre}</span>
                    </label>
                  );
                })}
              </div>
            </div>
          </section>

          <section className="paso">
            <h2 className="paso-titulo">
              <span className="paso-numero">3</span>
              Ubicación
            </h2>
            <div className="campo">
              <label htmlFor="departamento">Departamento</label>
              <input
                id="departamento"
                list="lista-departamentos"
                value={form.departamento}
                onChange={actualizar('departamento')}
                required
              />
              <datalist id="lista-departamentos">
                {DEPARTAMENTOS.map((d) => (
                  <option key={d} value={d} />
                ))}
              </datalist>
            </div>
            <div className="fila-2">
              <div className="campo">
                <label htmlFor="latitud">Latitud</label>
                <input
                  id="latitud"
                  type="number"
                  step="any"
                  min="-90"
                  max="90"
                  value={form.latitud}
                  onChange={actualizar('latitud')}
                  placeholder="5.6922"
                  required
                />
              </div>
              <div className="campo">
                <label htmlFor="longitud">Longitud</label>
                <input
                  id="longitud"
                  type="number"
                  step="any"
                  min="-180"
                  max="180"
                  value={form.longitud}
                  onChange={actualizar('longitud')}
                  placeholder="-76.6581"
                  required
                />
              </div>
            </div>
            <label className="interruptor">
              <input type="checkbox" checked={form.esSensible} onChange={actualizar('esSensible')} />
              <span className="interruptor-pista" aria-hidden="true" />
              <span>
                <strong>Especie sensible</strong>
                <small>Las coordenadas quedan ocultas para todos excepto la Dirección Científica.</small>
              </span>
            </label>
          </section>

          <section className="paso">
            <h2 className="paso-titulo">
              <span className="paso-numero">4</span>
              Ficha de {GRUPOS[grupo].nombre.toLowerCase()}
            </h2>
            <div className="fila-2">
              {requeridos.map((nombre) => {
                const info = infoAtributo(nombre);
                const numerico = info.tipo === 'DECIMAL' || info.tipo === 'ENTERO';
                return (
                  <div className="campo" key={nombre}>
                    <label htmlFor={`attr-${nombre}`}>
                      {info.etiqueta}
                      {info.unidad && <span className="texto-suave"> ({info.unidad})</span>}
                    </label>
                    <input
                      id={`attr-${nombre}`}
                      type={numerico ? 'number' : 'text'}
                      step={info.tipo === 'DECIMAL' ? 'any' : undefined}
                      value={valores[nombre] || ''}
                      onChange={(e) => setValores((v) => ({ ...v, [nombre]: e.target.value }))}
                      placeholder={info.ejemplo}
                      required
                    />
                  </div>
                );
              })}
            </div>
          </section>

          {error && <Aviso>{error}</Aviso>}

          <div className="formulario-acciones">
            <button
              type="button"
              className="btn btn-fantasma"
              onClick={() => {
                setForm(FORMULARIO_VACIO);
                setValores({});
                setError('');
              }}
            >
              Limpiar
            </button>
            <button type="submit" className="btn btn-primario" disabled={enviando}>
              <Icono nombre="mas" tamano={16} />
              {enviando ? 'Registrando…' : 'Registrar hallazgo'}
            </button>
          </div>
        </form>

        <aside className="panel panel-lista">
          <div className="panel-cabecera">
            <h2>Hallazgos registrados</h2>
            <span className="contador">{hallazgos.length}</span>
          </div>
          <label className="buscador buscador-pequeno">
            <Icono nombre="lupa" tamano={16} />
            <span className="sr-only">Buscar en los registros</span>
            <input
              type="search"
              placeholder="Buscar"
              value={busqueda}
              onChange={(e) => setBusqueda(e.target.value)}
            />
          </label>

          {cargando ? (
            <Cargando />
          ) : recientes.length === 0 ? (
            <Vacio titulo={busqueda ? 'Sin coincidencias' : 'Todavía no hay registros'}>
              {busqueda ? 'Prueba con otro nombre.' : 'Tu primer hallazgo aparecerá aquí.'}
            </Vacio>
          ) : (
            <ul className="lista-compacta">
              {recientes.map((h) => (
                <li key={h.id}>
                  <button onClick={() => setSeleccionado(h)}>
                    <span className="lista-compacta-principal">
                      <NombreCientifico>{h.nombreCientifico}</NombreCientifico>
                      <span className="texto-suave">
                        {h.nombreComun}, {h.departamento}
                      </span>
                    </span>
                    <span className="lista-compacta-lado">
                      <EtiquetaGrupo grupo={inferirGrupo(h.atributos)} />
                      <EtiquetaEstado codigo={h.estadoConservacion} />
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </aside>
      </div>

      <Modal abierto={Boolean(seleccionado)} onCerrar={() => setSeleccionado(null)} titulo="Ficha del hallazgo">
        {seleccionado && <FichaHallazgo hallazgo={seleccionado} />}
      </Modal>
    </Pagina>
  );
}
