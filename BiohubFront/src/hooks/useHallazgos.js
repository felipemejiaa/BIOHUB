import { useCallback, useEffect, useState } from 'react';
import { biohubApi } from '../api/biohubApi';

// Carga la lista de hallazgos (ya filtrada por el backend según el rol) y permite recargarla.
export function useHallazgos() {
  const [hallazgos, setHallazgos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const recargar = useCallback(async () => {
    try {
      const lista = await biohubApi.listarHallazgos();
      setHallazgos(lista);
      setError('');
      return lista;
    } catch (err) {
      setError(err.message);
      return [];
    } finally {
      setCargando(false);
    }
  }, []);

  useEffect(() => {
    let activo = true;
    biohubApi
      .listarHallazgos()
      .then((lista) => activo && setHallazgos(lista))
      .catch((err) => activo && setError(err.message))
      .finally(() => activo && setCargando(false));
    return () => {
      activo = false;
    };
  }, []);

  return { hallazgos, cargando, error, recargar };
}
