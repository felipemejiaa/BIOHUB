import { EscalaUICN, Icono, Logo, NombreCientifico } from './ui';

// Diseño a dos columnas para inicio de sesión y registro. A la izquierda, una ficha de
// ejemplo que resume lo que hace BioHub: registra, clasifica y protege la ubicación.
export function AuthLayout({ children }) {
  return (
    <div className="auth">
      <aside className="auth-lado">
        <Logo claro />

        <div className="auth-mensaje">
          <h1>El inventario nacional de flora y fauna, en un solo lugar.</h1>
          <p>
            Investigadores, directores y auditores registran y revisan cada hallazgo. El público consulta el
            catálogo sin poner en riesgo a las especies amenazadas.
          </p>
        </div>

        <figure className="ficha-muestra" aria-label="Ejemplo de ficha de hallazgo">
          <div className="ficha-muestra-cabecera">
            <span>Hallazgo 100</span>
            <span>Chocó, 15 ene 2026</span>
          </div>
          <NombreCientifico como="p" className="ficha-muestra-nombre">
            Dendrobates truncatus
          </NombreCientifico>
          <p className="ficha-muestra-comun">Rana venenosa</p>
          <EscalaUICN codigo="EN" />
          <dl className="ficha-muestra-datos">
            <div>
              <dt>Temperatura del agua</dt>
              <dd>18,5 °C</dd>
            </div>
            <div>
              <dt>Coordenadas</dt>
              <dd className="ficha-muestra-oculto">
                <Icono nombre="candado" tamano={13} />
                <span className="tachado">5.6922, -76.6581</span>
              </dd>
            </div>
          </dl>
          <figcaption>Las coordenadas exactas de especies sensibles solo las ve la Dirección Científica.</figcaption>
        </figure>
      </aside>

      <main className="auth-principal">{children}</main>
    </div>
  );
}
