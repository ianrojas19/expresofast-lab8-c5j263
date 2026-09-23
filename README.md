# ExpresoFast - Laboratorio 8

**Curso:** IF0009 - Desarrollo de Software IV  
**Ciclo:** II-2026  
**Laboratorio:** 8 - Integración Pila Completa (Full-Stack) - Consola de Operación Logística "ExpresoFast" con Backend REST, HTML5 Semántico y CSS3 Responsivo
**Estudiante:** Ian Rojas Sequeira  
**Carnet:** C5J263  

---

## Descripción

Cuarta parte de la plataforma logística ExpresoFast. Implementación de una Consola Web de Operación Logística conectando una API REST de Spring Boot con un Front-end usando HTML5 semántico, un diseño responsivo con CSS3 puro (Flexbox/Grid), y consumo asíncrono con JavaScript Fetch, manejando sesiones seguras mediante JWT.

---

## Requisitos de Entorno

| Herramienta | Versión |
|---|---|
| Java | 21 |
| Maven | 3.9+ |
| Microsoft SQL Server | Developer Edition 2019/2022 |
| Navegador | Chrome / Edge (con DevTools) |
| IDE | VS Code / IntelliJ IDEA |

---

## Estructura del Repositorio

```
expresofast-lab8-c5j263/
├── backend/          → Proyecto Spring Boot
│   ├── src/
│   └── pom.xml
├── database/
│   ├── 01_schema_lab5.sql           → Tablas base (Lab 5)
│   ├── 02_schema_lab6_extension.sql → Tablas nuevas (Lab 6)
│   └── 03_data_seeds.sql            → Datos de prueba con usuarios
├── frontend/
│   ├── index.html    → Dashboard principal
│   ├── login.html    → Pantalla de inicio de sesión
│   ├── styles.css    → Estilos
│   └── app.js        → Lógica JS con JWT
└── README.md
```

---

## Configuración de Base de Datos

### Paso 1: Ejecutar schema base (Lab 5)
```sql
-- En SSMS o sqlcmd:
sqlcmd -S localhost -E -i database/01_schema_lab5.sql
```

### Paso 2: Ejecutar extensión Lab 6
```sql
sqlcmd -S localhost -E -i database/02_schema_lab6_extension.sql
```

### Paso 3: Insertar usuarios iniciales
```sql
sqlcmd -S localhost -E -i database/03_data_seeds.sql
```

> **Nota:** Los hashes BCrypt en `03_data_seeds.sql` corresponden a la contraseña `Password123!` para todos los usuarios de prueba.

---

## Usuarios de Prueba

| Usuario | Contraseña | Rol | Permisos |
|---|---|---|---|
| admin | Password123! | ROLE_ADMIN | Todo: crear envíos, cambiar estado, ver bitácora, gestionar flota |
| operador1 | Password123! | ROLE_OPERADOR | Crear envíos, ver bitácora |
| conductor1 | Password123! | ROLE_CONDUCTOR | Ver envíos, cambiar estado |

---

## Instrucciones de Ejecución

### Backend (Spring Boot)

1. **Configurar credenciales de BD** en `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=ExpresoFastC5J263_II2026;trustServerCertificate=true;
   spring.datasource.username=expreso_user
   spring.datasource.password=Password123!
   ```

2. **Compilar y ejecutar:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   El servidor arranca en `http://localhost:8080`

### Frontend (HTML/JS)

Servir los archivos con un servidor HTTP local (python instalado requerido previamente):
```bash
cd frontend
python -m http.server 5500
# Alternativas: Usar extension de Visual Studio Code "Live Server"
```

Acceder en: `http://localhost:5500/`

---

## Endpoints de la API

| Método | Endpoint | Roles | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | Público | Autenticación y generación de JWT |
| GET | `/api/envios/optimizados` | ADMIN, OPERADOR, CONDUCTOR | Consulta del tablero |
| POST | `/api/envios` | ADMIN, OPERADOR | Registro de nuevo envío |
| PATCH | `/api/envios/{id}/estado` | ADMIN, CONDUCTOR | Cambio de estado (genera bitácora) |
| GET | `/api/envios/{id}/bitacora` | ADMIN, OPERADOR | Historial de auditoría |
| ALL | `/api/vehiculos/**` | ADMIN | Gestión de flota |

---

## Funcionalidades Implementadas

- **JWT + RBAC:** Autenticación stateless con tokens firmados HS384, control de acceso por rol
- **DTOs + Validación OWASP:** `EnvioRequestDTO` con `@NotBlank`, `@Pattern`, `@Positive`  
- **Bitácora automática:** Registro en `bitacora_envio` en cada cambio de estado con usuario, fecha y observaciones
- **Reglas de negocio:** Bloqueo de transiciones inválidas (ENTREGADO/CANCELADO → PENDIENTE/EN_TRANSITO)
- **GlobalExceptionHandler:** Respuestas JSON estandarizadas para 400, 401, 403, 404 y 500
- **Frontend protegido:** Login, localStorage JWT, fetchWithAuth(), renderizado condicional por rol
- **Modal de bitácora:** Con filtro por rango de fechas

---

## Ejecución de Pruebas Unitarias y Cobertura (Laboratorio 7)

Este proyecto cuenta con una suite de pruebas automatizadas y reportes de cobertura usando JaCoCo.

1. **Ejecutar pruebas y generar reporte de cobertura:**
   ```bash
   cd backend
   mvn clean verify
   ```
   *Nota:* Puedes usar `mvn clean test` para ejecutar solo las pruebas, pero se recomienda `verify` para comprobar las reglas de cobertura.

2. **Ver el reporte visual de JaCoCo:**
   Una vez ejecutado el comando anterior, abre el siguiente archivo en tu navegador web:
   ```text
   backend/target/site/jacoco/index.html
   ```
   Allí podrás ver el porcentaje de instrucciones cubiertas
