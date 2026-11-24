# BioHub

Sistema Nacional de Registro y Vigilancia de Biodiversidad.

Plataforma para el registro, consulta y auditoría de hallazgos de biodiversidad en Colombia, con fichas taxonómicas dinámicas y control de acceso basado en roles.

## Tecnologías

- **Backend:** Java 21, Spring Boot 4.1.2
- **Frontend:** React + Vite
- **Base de datos:** PostgreSQL 16
- **Contenedores:** Docker / Docker Compose

## Arquitectura

Monolito modular con los siguientes módulos:

| Módulo | Descripción |
|---|---|
| `seguridad` | Modelo de dominio: roles, hallazgos, especies, atributos dinámicos (JSONB) |
| `autenticacion` | Gestión de sesiones, usuarios, permisos por rol y falla cerrada |
| `auditoria` | Historial de reclasificaciones taxonómicas con patrón Memento |
| `hallazgos` | Lógica de negocio para registro y consulta de hallazgos |
| `persistencia` | Repositorios JDBC para PostgreSQL |
| `web` | Controladores REST (API Gateway) |

## Patrones de diseño

- **Strategy:** Visibilidad de ubicaciones según el rol del usuario (coordenadas exactas vs. solo departamento)
- **Factory Method:** Generación de fichas taxonómicas específicas por grupo biológico (anfibios, aves migratorias, reptiles, plantas)
- **Memento:** Historial auditable de reclasificaciones taxonómicas con snapshots inmutables

## Roles del sistema

| Rol | Permisos principales |
|---|---|
| Director Científico | Acceso total, reclasificar especies, auditar historial |
| Investigador de Campo | Registrar hallazgos, ver coordenadas exactas, reclasificar |
| Auditor Científico | Consultar historial de reclasificaciones |
| Consumidor Externo | Consumir API externa |
| Visitante Público | Solo consulta pública (falla cerrada) |

## Requisitos previos

- Java 21+
- Node.js 18+
- Docker Desktop

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/felipemejiaa/BIOHUB.git
cd BIOHUB
```

### 2. Configurar variables de entorno

Crear un archivo `.env` en la raíz del proyecto con las credenciales de la base de datos.

### 3. Levantar la base de datos

```bash
docker-compose up -d
```

### 4. Ejecutar el backend

```bash
./mvnw clean spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

El backend inicia en `http://localhost:8080`.

### 5. Ejecutar el frontend

```bash
cd BiohubFront
npm install
npm run dev
```

El frontend inicia en `http://localhost:5173`.

## Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Iniciar sesión |
| POST | `/api/auth/logout` | Cerrar sesión |
| GET | `/api/biohub/hallazgos` | Listar hallazgos |
| POST | `/api/biohub/hallazgos` | Registrar hallazgo |
| GET | `/api/biohub/fichas/{grupo}/requeridos` | Campos requeridos por grupo |
| POST | `/api/biohub/hallazgos/{id}/reclasificar` | Reclasificar especie |
| GET | `/api/biohub/hallazgos/{id}/historial` | Historial de reclasificaciones |

## Tests

```bash
./mvnw test
```

## Equipo

- Ramírez Laura
- Mesa Samuel
- Suarez David
- Acosta Samuel
- Mejía Felipe

## Universidad

Universidad El Bosque — Ingeniería de Software 2

Profesor: Jonathan Briceño
