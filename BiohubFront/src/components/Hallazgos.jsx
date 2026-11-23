import {
  atributosVisibles,
  formatearFecha,
  formatearFechaHora,
  infoAtributo,
  infoEstado,
  inferirGrupo,
  nombreRol,
} from '../utils/taxonomia';
import { EscalaUICN, EtiquetaEstado, EtiquetaGrupo, Icono, NombreCientifico, Ubicacion } from './ui';

export function TarjetaHallazgo({ hallazgo, onAbrir }) {
  const grupo = inferirGrupo(hallazgo.atributos);
  return (
    <article className="tarjeta-hallazgo">
      <button className="tarjeta-hallazgo-boton" onClick={() => onAbrir(hallazgo)}>
        <div className="tarjeta-hallazgo-meta">
          <EtiquetaGrupo grupo={grupo} />
          <span>{formatearFecha(hallazgo.fechaObservacion)}</span>
        </div>
        <NombreCientifico como="h3">{hallazgo.nombreCientifico}</NombreCientifico>
        <p className="tarjeta-hallazgo-comun">{hallazgo.nombreComun}</p>
        <EscalaUICN codigo={hallazgo.estadoConservacion} />
        <div className="tarjeta-hallazgo-pie">
          <Ubicacion hallazgo={hallazgo} />
        </div>
      </button>
    </article>
  );
}

export function FichaHallazgo({ hallazgo }) {
  const grupo = inferirGrupo(hallazgo.atributos);
  const visibles = atributosVisibles(hallazgo);
  const ocultos = (hallazgo.atributos || []).length - visibles.length;

  return (
    <div className="ficha">
      <div className="ficha-titulo">
        <NombreCientifico como="p" className="ficha-binomial">
          {hallazgo.nombreCientifico}
        </NombreCientifico>
        <p className="ficha-comun">{hallazgo.nombreComun}</p>
      </div>

      <EscalaUICN codigo={hallazgo.estadoConservacion} />

      <dl className="ficha-datos">
        <div>
          <dt>Registro</dt>
          <dd>Hallazgo {hallazgo.id}</dd>
        </div>
        <div>
          <dt>Grupo taxonómico</dt>
          <dd>{grupo ? <EtiquetaGrupo grupo={grupo} /> : '—'}</dd>
        </div>
        <div>
          <dt>Fecha de observación</dt>
          <dd>{formatearFechaHora(hallazgo.fechaObservacion)}</dd>
        </div>
        <div>
          <dt>Departamento</dt>
          <dd>{hallazgo.departamento}</dd>
        </div>
        <div className="ficha-datos-ancho">
          <dt>Ubicación</dt>
          <dd>
            <Ubicacion hallazgo={hallazgo} />
          </dd>
        </div>
      </dl>

      {hallazgo.esSensible && !hallazgo.coordenadasExactas && (
        <p className="nota-proteccion">
          <Icono nombre="candado" tamano={15} />
          Especie sensible. Solo la Dirección Científica puede ver dónde se encontró exactamente.
        </p>
      )}

      <h3 className="ficha-subtitulo">Datos de la ficha</h3>
      {visibles.length === 0 ? (
        <p className="texto-suave">Este hallazgo no tiene atributos visibles para tu rol.</p>
      ) : (
        <dl className="ficha-atributos">
          {visibles.map((a) => {
            const info = infoAtributo(a.nombre);
            return (
              <div key={a.nombre}>
                <dt>{info.etiqueta}</dt>
                <dd>
                  {a.valor}
                  {info.unidad && ` ${info.unidad}`}
                </dd>
              </div>
            );
          })}
        </dl>
      )}
      {ocultos > 0 && (
        <p className="texto-suave texto-pequeno">
          {ocultos === 1 ? 'Un atributo de ubicación está oculto' : `${ocultos} atributos de ubicación están ocultos`}{' '}
          para tu rol.
        </p>
      )}
    </div>
  );
}

const CAMPOS_COMPARADOS = [
  ['nombreCientifico', 'Nombre científico'],
  ['nombreComun', 'Nombre común'],
  ['estadoConservacion', 'Estado de conservación'],
];

const cambiosEntre = (anterior, actual) =>
  anterior
    ? CAMPOS_COMPARADOS.filter(
        ([campo]) => anterior.estadoHallazgo.especie[campo] !== actual.estadoHallazgo.especie[campo]
      ).map(([campo, etiqueta]) => ({
        etiqueta,
        antes: anterior.estadoHallazgo.especie[campo],
        despues: actual.estadoHallazgo.especie[campo],
      }))
    : [];

// Línea de tiempo del patrón Memento: cada versión es una foto inmutable del hallazgo.
export function LineaTiempo({ versiones }) {
  const ordenadas = [...versiones].sort((a, b) => b.version - a.version);

  return (
    <ol className="linea-tiempo">
      {ordenadas.map((m) => {
        const anterior = versiones.find((v) => v.version === m.version - 1);
        const cambios = cambiosEntre(anterior, m);
        const especie = m.estadoHallazgo.especie;
        const esInicial = m.version === 1;
        return (
          <li key={m.version} className={`version ${esInicial ? 'version-inicial' : ''}`}>
            <div className="version-marca" aria-hidden="true">
              v{m.version}
            </div>
            <div className="version-cuerpo">
              <div className="version-cabecera">
                <span className="version-fecha">{formatearFechaHora(m.fechaVersion)}</span>
                <EtiquetaEstado codigo={especie.estadoConservacion} />
              </div>
              <NombreCientifico como="h3">{especie.nombreCientifico}</NombreCientifico>
              <p className="version-comun">{especie.nombreComun}</p>

              {cambios.length > 0 && (
                <ul className="version-cambios">
                  {cambios.map((c) => (
                    <li key={c.etiqueta}>
                      <span className="texto-suave">{c.etiqueta}:</span> <del>{mostrarValor(c)}</del>{' '}
                      <ins>{mostrarValor(c, true)}</ins>
                    </li>
                  ))}
                </ul>
              )}

              <p className="version-motivo">{m.motivo}</p>
              <p className="version-autor">
                <Icono nombre="usuario" tamano={14} />
                <span>
                  {esInicial ? 'Registrado' : 'Autorizado'} por <strong>{m.autorizadoPor}</strong>,{' '}
                  {nombreRol(m.rolAutorizador).toLowerCase()}
                </span>
              </p>
            </div>
          </li>
        );
      })}
    </ol>
  );
}

const mostrarValor = (cambio, despues = false) => {
  const valor = despues ? cambio.despues : cambio.antes;
  return cambio.etiqueta === 'Estado de conservación' ? `${valor} (${infoEstado(valor).nombre})` : valor;
};
