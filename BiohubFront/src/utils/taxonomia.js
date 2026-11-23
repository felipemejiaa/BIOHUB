// Catálogos y utilidades compartidas por todas las páginas de BioHub.

// Categorías de la Lista Roja de la UICN, en el orden oficial y con sus colores oficiales.
export const ESCALA_UICN = [
  { codigo: 'EX', nombre: 'Extinta', color: '#000000', texto: '#ffffff' },
  { codigo: 'EW', nombre: 'Extinta en estado silvestre', color: '#542344', texto: '#ffffff' },
  { codigo: 'CR', nombre: 'En peligro crítico', color: '#D81E05', texto: '#ffffff' },
  { codigo: 'EN', nombre: 'En peligro', color: '#FC7F3F', texto: '#1b1b1b' },
  { codigo: 'VU', nombre: 'Vulnerable', color: '#F9E814', texto: '#1b1b1b' },
  { codigo: 'NT', nombre: 'Casi amenazada', color: '#CCE226', texto: '#1b1b1b' },
  { codigo: 'LC', nombre: 'Preocupación menor', color: '#60C659', texto: '#1b1b1b' },
];

// Estados que el sistema permite asignar hoy (los que acepta el backend en los formularios).
export const ESTADOS_ASIGNABLES = ['CR', 'EN', 'VU', 'LC'];

export const infoEstado = (codigo) =>
  ESCALA_UICN.find((e) => e.codigo === codigo) || {
    codigo: codigo || '—',
    nombre: 'Sin evaluar',
    color: '#c9cfc6',
    texto: '#1b1b1b',
  };

// Fichas taxonómicas (Factory Method en el backend). Los nombres de atributo deben
// coincidir exactamente con FichaAnfibio, FichaAveMigratoria, FichaPlanta y FichaReptil.
export const GRUPOS = {
  ANFIBIO: {
    nombre: 'Anfibio',
    descripcion: 'Ranas, sapos y salamandras',
    atributos: {
      temperaturaAgua: { etiqueta: 'Temperatura del agua', tipo: 'DECIMAL', unidad: '°C', ejemplo: '18.5' },
      grabacionAcustica: { etiqueta: 'Grabación acústica', tipo: 'TEXTO', ejemplo: 'canto_nocturno_01.wav' },
    },
  },
  AVE_MIGRATORIA: {
    nombre: 'Ave migratoria',
    descripcion: 'Especies con rutas estacionales',
    atributos: {
      rutaGps: { etiqueta: 'Ruta GPS', tipo: 'TEXTO', ejemplo: 'Páramo de Chingaza - Sector Norte', esUbicacion: true },
      muestraAdn: { etiqueta: 'Muestra de ADN', tipo: 'TEXTO', ejemplo: 'ADN-VG-2026-88' },
    },
  },
  PLANTA: {
    nombre: 'Planta',
    descripcion: 'Flora vascular y no vascular',
    atributos: {
      alturaTallo: { etiqueta: 'Altura del tallo', tipo: 'DECIMAL', unidad: 'cm', ejemplo: '42' },
      muestraHerbario: { etiqueta: 'Muestra de herbario', tipo: 'TEXTO', ejemplo: 'HERB-COL-0912' },
    },
  },
  REPTIL: {
    nombre: 'Reptil',
    descripcion: 'Serpientes, lagartos y tortugas',
    atributos: {
      longitudCorporal: { etiqueta: 'Longitud corporal', tipo: 'DECIMAL', unidad: 'cm', ejemplo: '65' },
      fotoEscamas: { etiqueta: 'Foto de escamas', tipo: 'TEXTO', ejemplo: 'escamas_dorsal_03.jpg' },
    },
  },
};

const todosLosAtributos = Object.values(GRUPOS).reduce(
  (acc, g) => ({ ...acc, ...g.atributos }),
  {}
);

export const infoAtributo = (nombre) =>
  todosLosAtributos[nombre] || { etiqueta: nombre, tipo: 'TEXTO' };

// El DTO de hallazgo no trae el grupo: se deduce por los atributos de su ficha.
export const inferirGrupo = (atributos = []) => {
  const nombres = atributos.map((a) => a.nombre);
  const encontrado = Object.entries(GRUPOS).find(([, g]) =>
    Object.keys(g.atributos).some((n) => nombres.includes(n))
  );
  return encontrado ? encontrado[0] : null;
};

// ASR-01: los atributos de tipo ubicación (rutas GPS, coordenadas) solo se muestran
// a quien recibe coordenadas exactas.
export const atributosVisibles = (hallazgo) =>
  (hallazgo.atributos || []).filter(
    (a) => hallazgo.coordenadasExactas || !(infoAtributo(a.nombre).esUbicacion || /gps|coord/i.test(a.nombre))
  );

export const ROLES = {
  DIRECTOR_CIENTIFICO: {
    nombre: 'Director científico',
    descripcion: 'Ve coordenadas exactas, autoriza reclasificaciones y revisa el historial.',
  },
  INVESTIGADOR_CAMPO: {
    nombre: 'Investigador de campo',
    descripcion: 'Registra hallazgos con la ficha que corresponde a cada grupo taxonómico.',
  },
  AUDITOR_CIENTIFICO: {
    nombre: 'Auditor científico',
    descripcion: 'Reconstruye y compara las versiones de cada hallazgo.',
  },
  CONSUMIDOR_EXTERNO: {
    nombre: 'Consumidor externo',
    descripcion: 'ONG o universidad que consulta el catálogo público.',
  },
  VISITANTE_PUBLICO: {
    nombre: 'Visitante',
    descripcion: 'Consulta pública sin iniciar sesión.',
  },
};

export const nombreRol = (rol) => ROLES[rol]?.nombre || rol;

const formatoFecha = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short', year: 'numeric' });
const formatoFechaHora = new Intl.DateTimeFormat('es-CO', {
  day: 'numeric',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
});

export const formatearFecha = (iso) => (iso ? formatoFecha.format(new Date(iso)) : '—');
export const formatearFechaHora = (iso) => (iso ? formatoFechaHora.format(new Date(iso)) : '—');

// Convierte un valor de <input type="datetime-local"> al formato que espera LocalDateTime.parse.
export const aLocalDateTime = (valor) => (valor && valor.length === 16 ? `${valor}:00` : valor);

// Fecha y hora actual en el formato de <input type="datetime-local">.
export const ahoraLocal = () => {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
};

export const normalizar = (texto = '') =>
  texto.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();
