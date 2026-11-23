/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useState } from 'react';
import { Icono } from '../components/ui';

const ToastContext = createContext(() => {});

export function ToastProvider({ children }) {
  const [avisos, setAvisos] = useState([]);

  const notificar = useCallback((texto, tipo = 'ok') => {
    const id = Date.now() + Math.random();
    setAvisos((lista) => [...lista, { id, texto, tipo }]);
    setTimeout(() => setAvisos((lista) => lista.filter((a) => a.id !== id)), 4500);
  }, []);

  return (
    <ToastContext.Provider value={notificar}>
      {children}
      <div className="toasts" aria-live="polite">
        {avisos.map((a) => (
          <div key={a.id} className={`toast toast-${a.tipo}`}>
            <Icono nombre={a.tipo === 'error' ? 'alerta' : 'check'} tamano={16} />
            {a.texto}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export const useToast = () => useContext(ToastContext);
