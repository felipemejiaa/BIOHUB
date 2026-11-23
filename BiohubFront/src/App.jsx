import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { RutaProtegida } from './components/RutaProtegida';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import { AuditorPage } from './pages/AuditorPage';
import { DirectorPage } from './pages/DirectorPage';
import { InvestigadorPage } from './pages/InvestigadorPage';
import { LoginPage } from './pages/LoginPage';
import { PublicoPage } from './pages/PublicoPage';
import { RegistroPage } from './pages/RegistroPage';

export function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<LoginPage />} />
            <Route path="/registro" element={<RegistroPage />} />
            <Route path="/publico" element={<PublicoPage />} />
            <Route
              path="/investigador"
              element={
                <RutaProtegida rolRequerido="INVESTIGADOR_CAMPO">
                  <InvestigadorPage />
                </RutaProtegida>
              }
            />
            <Route
              path="/director"
              element={
                <RutaProtegida rolRequerido="DIRECTOR_CIENTIFICO">
                  <DirectorPage />
                </RutaProtegida>
              }
            />
            <Route
              path="/auditor"
              element={
                <RutaProtegida rolRequerido="AUDITOR_CIENTIFICO">
                  <AuditorPage />
                </RutaProtegida>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </ToastProvider>
    </AuthProvider>
  );
}

export default App;
