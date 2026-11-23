import { useEffect, useRef } from 'react';
import { ESCALA_UICN, GRUPOS, infoEstado } from '../utils/taxonomia';

const TRAZOS = {
  lupa: 'M11 19a8 8 0 1 1 0-16 8 8 0 0 1 0 16Zm10 2-4.35-4.35',
  candado: 'M7 11V8a5 5 0 0 1 10 0v3M5 11h14v10H5z',
  pin: 'M12 22s7-6.2 7-12a7 7 0 1 0-14 0c0 5.8 7 12 7 12Zm0-9a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z',
  salir: 'M15 4h4v16h-4M10 17l5-5-5-5M15 12H3',
  entrar: 'M15 4h4v16h-4M10 17l5-5-5-5M15 12H3',
  mas: 'M12 5v14M5 12h14',
  reloj: 'M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Zm0-13v4l3 2',
  check: 'M5 12.5 10 17l9-10',
  alerta: 'M12 9v4m0 4h.01M10.3 3.9 2.4 18a2 2 0 0 0 1.7 3h15.8a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z',
  cerrar: 'M6 6l12 12M18 6 6 18',
  ojo: 'M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12Zm10 3a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z',
  ojoNo: 'M3 3l18 18M10.6 5.1A10 10 0 0 1 12 5c6.4 0 10 7 10 7a17 17 0 0 1-3.2 4M6.6 6.6A17 17 0 0 0 2 12s3.6 7 10 7a9.7 9.7 0 0 0 5.4-1.6M9.9 9.9a3 3 0 0 0 4.2 4.2',
  historial: 'M3 12a9 9 0 1 0 3-6.7L3 8m0-5v5h5m4-1v5l3 2',
  editar: 'M4 20h4L19 9l-4-4L4 16v4ZM14 6l4 4',
  hoja: 'M5 19c0-8 5-14 15-15-1 10-7 15-15 15Zm0 0 7-7',
  comparar: 'M8 3v18M16 3v18M3 8h5M16 16h5',
  usuario: 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm-7 9a7 7 0 0 1 14 0',
};

export function Icono({ nombre, tamano = 18, className = '' }) {
  return (
    <svg
      className={`icono ${className}`}
      width={tamano}
      height={tamano}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={TRAZOS[nombre]} />
    </svg>
  );
}

export function Logo({ claro = false }) {
  return (
    <span className={`logo ${claro ? 'logo-claro' : ''}`}>
      <svg width="26" height="26" viewBox="0 0 32 32" aria-hidden="true">
        <path d="M6 26C6 14 13 6 27 5c-1 14-9 21-21 21Z" fill="currentColor" opacity=".9" />
        <path d="M6 26 18 14" stroke="var(--logo-vena)" strokeWidth="2" strokeLinecap="round" />
      </svg>
      <span>BioHub</span>
    </span>
  );
}

// Nombre científico: por convención de nomenclatura binomial va siempre en cursiva.
export function NombreCientifico({ children, como: Etiqueta = 'span', className = '' }) {
  return <Etiqueta className={`binomial ${className}`}>{children}</Etiqueta>;
}

// Escala de la Lista Roja de la UICN con la categoría actual resaltada.
export function EscalaUICN({ codigo, compacta = false }) {
  const actual = infoEstado(codigo);
  return (
    <div className={`uicn ${compacta ? 'uicn-compacta' : ''}`} title={`${actual.codigo}: ${actual.nombre}`}>
      <ol className="uicn-escala" aria-label={`Estado de conservación: ${actual.nombre}`}>
        {ESCALA_UICN.map((e) => {
          const activo = e.codigo === codigo;
          return (
            <li
              key={e.codigo}
              className={activo ? 'activo' : ''}
              style={activo ? { background: e.color, color: e.texto, borderColor: e.color } : undefined}
              aria-current={activo ? 'true' : undefined}
            >
              {e.codigo}
            </li>
          );
        })}
      </ol>
      {!compacta && <span className="uicn-nombre">{actual.nombre}</span>}
    </div>
  );
}

export function EtiquetaEstado({ codigo }) {
  const e = infoEstado(codigo);
  return (
    <span className="etiqueta-estado" style={{ background: e.color, color: e.texto }} title={e.nombre}>
      {e.codigo}
    </span>
  );
}

export function EtiquetaGrupo({ grupo }) {
  if (!grupo) return null;
  return <span className="etiqueta-grupo">{GRUPOS[grupo]?.nombre || grupo}</span>;
}

// ASR-01: si el rol no recibe coordenadas exactas, se muestra el dato tachado.
export function Ubicacion({ hallazgo }) {
  if (hallazgo.coordenadasExactas) {
    return (
      <span className="ubicacion">
        <Icono nombre="pin" tamano={15} />
        {hallazgo.ubicacionVisible}
      </span>
    );
  }
  return (
    <span className="ubicacion">
      <Icono nombre="pin" tamano={15} />
      {hallazgo.ubicacionVisible}
      {hallazgo.esSensible && (
        <span className="coordenadas-ocultas" title="Coordenadas exactas visibles solo para Dirección Científica">
          <Icono nombre="candado" tamano={12} />
          <span className="tachado" aria-hidden="true">
            0.0000, -00.0000
          </span>
          <span className="sr-only">Coordenadas protegidas</span>
        </span>
      )}
    </span>
  );
}

export function Vacio({ titulo, children, accion }) {
  return (
    <div className="vacio">
      <Icono nombre="hoja" tamano={28} />
      <p className="vacio-titulo">{titulo}</p>
      {children && <p className="vacio-texto">{children}</p>}
      {accion}
    </div>
  );
}

export function Cargando({ filas = 3 }) {
  return (
    <div className="cargando" aria-busy="true" aria-label="Cargando">
      {Array.from({ length: filas }).map((_, i) => (
        <div key={i} className="cargando-fila" />
      ))}
    </div>
  );
}

export function Aviso({ tipo = 'error', children }) {
  return (
    <div className={`aviso aviso-${tipo}`} role={tipo === 'error' ? 'alert' : 'status'}>
      <Icono nombre={tipo === 'error' ? 'alerta' : 'check'} tamano={16} />
      <div>{children}</div>
    </div>
  );
}

export function Modal({ abierto, onCerrar, titulo, children, ancho = 640 }) {
  const ref = useRef(null);

  useEffect(() => {
    const dialogo = ref.current;
    if (!dialogo) return;
    if (abierto && !dialogo.open) dialogo.showModal();
    if (!abierto && dialogo.open) dialogo.close();
  }, [abierto]);

  return (
    <dialog
      ref={ref}
      className="modal"
      style={{ maxWidth: ancho }}
      onClose={onCerrar}
      onClick={(e) => {
        if (e.target === ref.current) onCerrar();
      }}
    >
      {abierto && (
        <>
          <header className="modal-cabecera">
            <h2>{titulo}</h2>
            <button className="btn-icono" onClick={onCerrar} aria-label="Cerrar">
              <Icono nombre="cerrar" />
            </button>
          </header>
          <div className="modal-cuerpo">{children}</div>
        </>
      )}
    </dialog>
  );
}

export function Pestanas({ opciones, valor, onCambiar }) {
  return (
    <div className="pestanas" role="tablist">
      {opciones.map((o) => (
        <button
          key={o.valor}
          role="tab"
          aria-selected={valor === o.valor}
          className={valor === o.valor ? 'activa' : ''}
          onClick={() => onCambiar(o.valor)}
        >
          {o.etiqueta}
          {o.cuenta !== undefined && <span className="pestana-cuenta">{o.cuenta}</span>}
        </button>
      ))}
    </div>
  );
}

export function Cifra({ valor, etiqueta, detalle }) {
  return (
    <div className="cifra">
      <span className="cifra-valor">{valor}</span>
      <span className="cifra-etiqueta">{etiqueta}</span>
      {detalle && <span className="cifra-detalle">{detalle}</span>}
    </div>
  );
}

// Barra apilada con la distribución de hallazgos por categoría UICN.
export function DistribucionUICN({ hallazgos }) {
  const total = hallazgos.length;
  const conteo = ESCALA_UICN.map((e) => ({
    ...e,
    cantidad: hallazgos.filter((h) => h.estadoConservacion === e.codigo).length,
  })).filter((e) => e.cantidad > 0);

  if (total === 0) return null;

  return (
    <div className="distribucion">
      <div className="distribucion-barra" role="img" aria-label="Distribución por estado de conservación">
        {conteo.map((e) => (
          <span
            key={e.codigo}
            style={{ flexGrow: e.cantidad, background: e.color }}
            title={`${e.nombre}: ${e.cantidad}`}
          />
        ))}
      </div>
      <ul className="distribucion-leyenda">
        {conteo.map((e) => (
          <li key={e.codigo}>
            <span className="punto" style={{ background: e.color }} />
            {e.nombre}
            <strong>{e.cantidad}</strong>
          </li>
        ))}
      </ul>
    </div>
  );
}
