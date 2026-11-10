# TallerTech Backend — Etapas 1 y 2

Proyecto independiente de Android: Java 21, Spring Boot 3.5.7, Maven 3.9.9, Spring Web, Spring Data JPA, MariaDB y Bean Validation. Room permanece independiente; Android incorpora Retrofit en Etapa 2.

## Requisitos y MariaDB

- JDK 21 (JAVA_HOME debe apuntar al JDK; verificar con `java -version`).
- MariaDB 10.11 o posterior, activo y accesible por JDBC.
- Maven se descarga mediante el Wrapper; no requiere instalación global.
- Puerto HTTP predeterminado: 8080.

En una sesión administrativa de MariaDB, crear una base dedicada de desarrollo:

```sql
CREATE DATABASE IF NOT EXISTS tallertech CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'tallertech_app'@'localhost' IDENTIFIED BY '<CONTRASENA_LOCAL>';
GRANT ALL PRIVILEGES ON tallertech.* TO 'tallertech_app'@'localhost';
```

Sustituir el marcador solo en la consola local. No guardar credenciales en archivos versionados. Estos privilegios permiten que Hibernate cree/actualice tablas en esta base de desarrollo; no son una política de producción.

En PowerShell, desde `backend/`:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
$env:DB_URL='jdbc:mariadb://localhost:3306/tallertech'
$env:DB_USERNAME='tallertech_app'
# Introducir DB_PASSWORD en la sesión local, sin guardarlo en Git.
$env:DB_PASSWORD=[System.Net.NetworkCredential]::new('', (Read-Host 'Contraseña MariaDB' -AsSecureString)).Password
.\mvnw.cmd spring-boot:run
```

Variables disponibles:

| Variable | Predeterminado |
| --- | --- |
| DB_URL | jdbc:mariadb://localhost:3306/tallertech |
| DB_USERNAME | root |
| DB_PASSWORD | vacío |
| PORT | 8080 |

`ddl-auto=update` crea/actualiza el esquema para desarrollo. No elimina tablas ni datos; revisar cambios antes de utilizar una base existente. Migraciones versionadas y endurecimiento de configuración quedan pendientes para una etapa de producción. `open-in-view=false`; nunca se serializan entidades JPA.

## Compilar y probar

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd test
java -jar target/tallertech-backend-0.0.1-SNAPSHOT.jar
```

El paquete ejecutable se crea en `target/`. Las pruebas usan H2 **solo en test**, en modo MariaDB, con base en memoria aislada y transacciones revertidas. Comprueban HTTP → Controller → Service → Repository → JPA y restricciones SQL. No demuestran compatibilidad real con MariaDB; esta debe verificarse manualmente con la configuración anterior.

El Wrapper oficial 3.3.4 incluye un ajuste puntual en Windows para manejar directorios cuyo atributo PowerShell `Target` es nulo. Las versiones y URLs de descarga son oficiales. Referencias: [Spring Boot](https://docs.spring.io/spring-boot/3.5/system-requirements.html), [Maven Wrapper](https://maven.apache.org/tools/wrapper/).

## Estructura

```text
src/main/java/com/tallertech/backend/
  TallerTechBackendApplication.java
  controller/       REST y códigos HTTP
  dto/              Request y Response por recurso
  entity/           JPA y BaseEntity (id, updatedAt)
  exception/        Excepciones y errores JSON globales
  repository/       Spring Data JPA
  service/          Interfaces, validación de IDs
  service/impl/     Transacciones y reglas de negocio
src/main/resources/application.properties
src/test/           Pruebas de integración con MockMvc/H2
postman/TallerTech.postman_collection.json
```

## Modelo derivado de Room

Todos los recursos tienen `id` string UUID y `updatedAt` numérico (epoch en milisegundos). Se preserva exactamente el UUID suministrado; si se omite (o es null), se genera uno. POST con ID existente da 409, nunca reemplaza. PUT no crea registros y no permite cambiar el ID.

BaseEntity implementa Persistable: un objeto nuevo con UUID asignado se inserta mediante persist, evitando que save utilice merge y reemplace una fila ante una carrera de POST con el mismo ID. Las filas recuperadas se marcan como existentes mediante PostLoad.

| Entidad | Campos propios |
| --- | --- |
| Client | name, phone, email, address, notes |
| User | name, email, role |
| Equipment | clientId, type, brand, model, serial, description |
| ServiceOrder | code, clientId, equipmentId, issue, entryDate, technician, status |
| Diagnosis | orderId, diagnosis, observations, recommendations |
| Repair | orderId, repair, parts, observations |

`entryDate` también es epoch en milisegundos. POST conserva `updatedAt` recibido; si falta, usa la hora del servidor. PUT asigna la hora del servidor, garantizando avance respecto del valor anterior. No hay resolución de conflictos ni sincronización.

Relaciones JPA sin cascadas de eliminación: Equipment → Client (ManyToOne), ServiceOrder → Client y Equipment (ManyToOne), Diagnosis/Repair → ServiceOrder (OneToOne). Los DTO usan IDs planos, sin grafos recursivos. Las FK están en la base. Serial, code y email de User son únicos; orderId es único en Diagnosis y Repair. Un equipo con órdenes no puede cambiar de cliente, para preservar la coherencia de órdenes existentes. La creación/actualización de órdenes y la actualización de equipo bloquean el equipo durante la transacción.

No se incluye `syncStatus`: es exclusivamente local. User no contiene password en Room. El backend agrega password_hash BCrypt, excluido de los DTO y de JSON; los CRUD de usuarios siguen aceptando solo datos públicos. Los campos JSON desconocidos se rechazan con 400.

Campos requeridos: Client name/phone (coinciden con el formulario Android), Equipment clientId, ServiceOrder code/clientId/equipmentId/entryDate/status, Diagnosis y Repair orderId. Otros campos son opcionales. Email se valida si está presente; el texto vacío es válido para un email opcional. User conserva opcionalidad del modelo Room. No se inventan roles.

Estados exactos de Android: `Pendiente`, `Diagnóstico`, `En reparación`, `Completado`.
UUID con guiones; fechas no negativas. Campos cortos admiten hasta 255 caracteres y textos largos hasta 10000. Los textos largos se almacenan como TEXT. Para un serial/email opcional no conocido, preferir null u omitirlo: cadena vacía es un valor y participa de la unicidad. La comparación de valores únicos en MariaDB sigue la colación configurada (predeterminada de ejemplo: insensible a mayúsculas).

## Endpoints

Todas las respuestas son JSON salvo el cuerpo vacío de errores HTTP que no sean procesados por Spring MVC.

| Recurso | Endpoints |
| --- | --- |
| Clients | GET /api/clients; GET /api/clients/{id}; POST /api/clients; PUT /api/clients/{id} |
| Equipment | GET /api/equipment; GET /api/equipment/{id}; GET /api/equipment/client/{clientId}; POST /api/equipment; PUT /api/equipment/{id} |
| Orders | GET /api/orders; GET /api/orders/{id}; GET /api/orders/client/{clientId}; GET /api/orders/equipment/{equipmentId}; POST /api/orders; PUT /api/orders/{id} |
| Diagnoses | GET /api/diagnoses; GET /api/diagnoses/{id}; GET /api/diagnoses/order/{orderId}; POST /api/diagnoses; PUT /api/diagnoses/{id} |
| Repairs | GET /api/repairs; GET /api/repairs/{id}; GET /api/repairs/order/{orderId}; POST /api/repairs; PUT /api/repairs/{id} |
| Users | GET /api/users; GET /api/users/{id}; POST /api/users; PUT /api/users/{id} |

PUT reemplaza todos los campos de negocio; campos opcionales omitidos quedan null. El id del cuerpo es opcional, pero si aparece debe coincidir con la ruta. Actualizar estado se realiza mediante PUT completo de la orden; no se agrega PATCH en esta etapa.

GET de colecciones devuelve una lista (vacía si no hay registros); filtros con padre inexistente devuelven 404. Diagnóstico/reparación por orden devuelve un objeto o 404 si todavía no existe.

HTTP: 200 consulta/actualización, 201 creación con Location, 400 entrada inválida, 404 recurso inexistente, 409 duplicado o relación incompatible. Las excepciones de integridad SQL también producen 409 para cubrir carreras de unicidad. Errores internos producen 500 sin detalles al cliente.

```json
{"status":404,"message":"Cliente no encontrado"}
```

## Postman y verificación manual real

Importar `postman/TallerTech.postman_collection.json` mediante Import en Postman. Configurar la variable de colección `baseUrl`. Primero ejecutar auth/Login (configuración debajo). Después ejecutar las carpetas en este orden: clients, equipment, orders, diagnoses, repairs; users es independiente. Los POST guardan los IDs en variables. Los ejemplos usan IDs estables y datos ficticios; repetir un POST debe producir 409. Para nuevas pruebas cambiar ID y campos únicos, sin borrar bases existentes.

1. Arrancar contra MariaDB y confirmar que Hibernate crea seis tablas, FK e índices únicos.
2. Crear cliente, equipo, orden, diagnóstico, reparación y usuario.
3. Consultar listas, IDs y filtros, y actualizar mediante PUT.
4. Verificar preservación exacta de UUID y updatedAt en POST; avance de updatedAt en PUT.
5. Repetir serial/code/email y diagnóstico/reparación de una misma orden: 409.
6. Usar padre inexistente: 404; usar equipo de otro cliente: 409.
7. Probar validación de email, UUID, estado, campos requeridos y JSON mal formado: 400.
8. Reiniciar Spring Boot y confirmar persistencia de los datos en MariaDB.
9. Revisar restricciones con acceso SQL local y probar escrituras concurrentes de códigos/seriales únicos.

## Etapa 2: autenticación JWT

Spring Security 6.5 (gestionado por Spring Boot 3.5.7), BCrypt y JJWT 0.13.0.
Se utiliza la API actual `Jwts.parser().verifyWith(...).build().parseSignedClaims(...)`, compatible con Java 21,
en lugar de las APIs obsoletas de guías antiguas. Referencias oficiales:
[Spring Security](https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html),
[JJWT](https://github.com/jwtk/jjwt), [Retrofit](https://square.github.io/retrofit/),
[Android Keystore](https://developer.android.com/privacy-and-security/keystore).

| Variable | Uso |
| --- | --- |
| JWT_SECRET | Obligatoria: clave aleatoria Base64 de al menos 32 bytes; sin valor predeterminado |
| JWT_EXPIRATION_MS | Tiempo de vida positivo en milisegundos; predeterminado 3600000 |
| BOOTSTRAP_ADMIN_NAME | Nombre público de la cuenta inicial |
| BOOTSTRAP_ADMIN_EMAIL | Email válido de la cuenta inicial |
| BOOTSTRAP_ADMIN_PASSWORD | Contraseña inicial, mínimo 12 caracteres y máximo 72 bytes UTF-8 (límite BCrypt) |
| BOOTSTRAP_ADMIN_ROLE | Rol público existente, por ejemplo Técnico; no se aplica jerarquía de roles |

Conservar JWT_SECRET fuera de Git; cambiarla invalida todos los tokens existentes. El backend falla al arrancar si no hay una clave válida.
El secreto de test está exclusivamente en application-test.properties. No usar el perfil test para ejecutar contra datos reales.

Bootstrap está desactivado si las cuatro variables están vacías. Si se configura alguna, se requieren todas.
Crea un UUID y un hash BCrypt solo si el email no existe. No duplica, no restablece passwords y no habilita automáticamente usuarios antiguos.
Retirar BOOTSTRAP_ADMIN_PASSWORD del entorno tras la creación. Los usuarios antiguos conservan password_hash null y no pueden iniciar sesión.
Los endpoints /api/users siguen gestionando datos públicos; no constituyen un registro ni una interfaz para asignar contraseñas.
La cuenta de desarrollo se crea exclusivamente con bootstrap; usar otro email nuevo si ya existe un registro sin password.
Hibernate agrega una columna nullable password_hash VARCHAR(60), conservando los registros existentes. No se modifican tablas Room.

SecurityFilterChain es STATELESS, sin sesión HTTP, Basic, form login ni logout de servidor.
CSRF desactivado para esta API Bearer sin cookies; no se agrega CORS porque Android nativo no lo necesita.
POST /api/auth/login es el único endpoint público. Todo lo demás requiere autenticación.
AuthenticationManager y DaoAuthenticationProvider validan UserDetailsService + BCrypt.
JwtAuthenticationFilter valida firma HS256, emisor tallertech y expiración, y vuelve a comprobar que el usuario existe y tiene hash.
El subject es userId; claims adicionales email y role, sin passwords. Cada solicitud usa un contexto de autenticación nuevo.
No hay permisos finos por rol; todas las cuentas autenticadas tienen acceso a los recursos de esta etapa.

```http
POST /api/auth/login
Content-Type: application/json

{"email":"<EMAIL_LOCAL>","password":"<CONTRASENA_LOCAL>"}
```

200 devuelve token, tokenType Bearer, expiresIn en milisegundos y user público (id, name, email, role, updatedAt).
401 devuelve `{"status":401,"message":"Credenciales inválidas"}` tanto para email desconocido como contraseña incorrecta.
Un cuerpo inválido devuelve 400. No se registran credenciales ni tokens.

```http
GET /api/auth/me
Authorization: Bearer <TOKEN_RECIBIDO>
```

200 devuelve el usuario público actual. Sin token, con token inválido/expirado o usuario eliminado: 401 JSON.
El manejador de acceso denegado devuelve 403 JSON, sin HTML ni detalles internos; no hay restricciones específicas por rol aún.
No existe refresh token. Cerrar sesión elimina el token Android, pero un token copiado sigue válido hasta expirar.

## Android y capa remota

Retrofit 3.0.0, converter-gson 3.0.0 y OkHttp 4.12.0. Todo el código de aplicación nuevo es Java.
`data/remote/api/TallerTechApi` representa auth y los GET/POST/PUT y filtros existentes de los cinco recursos operativos.
DTO Request/Response separados de Room en dto/auth, clients, equipment, orders, diagnoses y repairs.
Solo AuthRepository usa funcionalmente Retrofit. Los repositorios de Clients, Equipment, Orders, Diagnosis y Repair permanecen locales.
200/201 son respuestas exitosas; ApiErrors centraliza mensajes para 400/401/403/404/409/5xx, conexión y timeouts.
No se muestran cuerpos de error internos del servidor ni stack traces.

RetrofitClient centraliza BuildConfig.API_BASE_URL. Debug: http://10.0.2.2:8080/.
10.0.2.2 representa la PC host desde Android Emulator; localhost en el emulador representa al propio emulador.
Release usa un marcador HTTPS no operativo (https://api.tallertech.invalid/); sustituirlo con
`-PapiBaseUrl=https://host/` cuando exista el servicio productivo. No hay despliegue en esta etapa.
HTTP se permite solo en debug y para los dominios 10.0.2.2 y localhost mediante network-security-config.
Release prohíbe cleartext. Para teléfono físico se necesitará configurar explícitamente una URL accesible y su política de debug.

AuthInterceptor añade Bearer cuando hay token, omite login y elimina el token en respuestas 401 protegidas.
No hay interceptor de logging, redirecciones automáticas ni reintentos infinitos.
Timeouts: conexión 10 s, lectura/escritura 15 s, llamada completa 25 s; reintento automático desactivado.

TokenManager cifra JWT con AES-256/GCM, IV aleatorio y clave no exportable de Android Keystore.
SharedPreferences privado solo almacena ciphertext e IV; está excluido de backup cloud y transferencia de dispositivo.
Nunca se guarda token ni contraseña en Room. Los fallos de descifrado eliminan la sesión.
Las operaciones de almacenamiento de autenticación se ejecutan en un executor; Retrofit es asíncrono.
LoginViewModel conserva una sola solicitud al recrear la Activity. El botón se deshabilita y muestra Conectando durante la solicitud.
Profile muestra datos públicos y Cerrar sesión elimina el token y vacía la pila de navegación, sin borrar Room.

En cada apertura por LoginActivity se intenta GET /api/auth/me antes de entrar.
401 elimina token y exige nuevo login. Otros errores HTTP no abren la sesión ni eliminan indiscriminadamente el token.
Si hay un fallo de transporte y un JWT previamente recibido y guardado cifrado todavía no ha expirado,
se permite acceso **solo a la UI local Room**, mostrando aviso de modo sin conexión.
Esta comprobación local lee emisor/expiración de ese token protegido: no verifica firma ni autentica contra el servidor;
la API siempre verifica el JWT. El perfil offline usa el email como nombre hasta la siguiente validación remota.
Un token vencido o corrupto no habilita esta continuidad local. No se considera una sesión remota validada indefinidamente.
Una sesión ya abierta puede seguir operando con Room sin conexión; los CRUD no dependen del servidor.
MainActivity exige una sesión pública en memoria; tras muerte del proceso vuelve por Login y valida nuevamente.

## Prueba manual de Etapa 2 (preparada, no ejecutada por el agente)

1. Iniciar el servicio MariaDB y verificar que la base tallertech y un usuario dedicado existen. Usar la configuración SQL anterior si se necesita una base nueva; no borrar la existente.
2. En PowerShell desde backend configurar JAVA_HOME y DB_URL/DB_USERNAME/DB_PASSWORD con las credenciales locales. No imprimirlas ni exportarlas a archivos versionados.
3. Generar JWT_SECRET localmente sin imprimirla:

   ```powershell
   $env:JAVA_HOME='C:\Program Files\Java\jdk-21'
   $env:DB_URL='jdbc:mariadb://localhost:3306/tallertech'
   $env:DB_USERNAME='tallertech_app'
   $env:DB_PASSWORD=[System.Net.NetworkCredential]::new('', (Read-Host 'Contraseña MariaDB' -AsSecureString)).Password
   $jwtBytes=New-Object byte[] 32
   $jwtRandom=[Security.Cryptography.RandomNumberGenerator]::Create()
   $jwtRandom.GetBytes($jwtBytes)
   $env:JWT_SECRET=[Convert]::ToBase64String($jwtBytes)
   $jwtRandom.Dispose()
   $env:JWT_EXPIRATION_MS='3600000'
   ```

4. Configurar bootstrap con datos locales. Si el email ya existe, no se modifica; usar un email nuevo para esta cuenta de pruebas.

   ```powershell
   $env:BOOTSTRAP_ADMIN_NAME=Read-Host 'Nombre inicial'
   $env:BOOTSTRAP_ADMIN_EMAIL=Read-Host 'Email inicial'
   $env:BOOTSTRAP_ADMIN_PASSWORD=[System.Net.NetworkCredential]::new('', (Read-Host 'Contraseña inicial (12 caracteres mínimo)' -AsSecureString)).Password
   $env:BOOTSTRAP_ADMIN_ROLE='Técnico'
   ```

5. Ejecutar `.\mvnw.cmd spring-boot:run`. Confirmar arranque en 8080 sin errores. Reiniciar con las mismas variables para comprobar que no se duplica la cuenta. Eliminar la variable de password bootstrap después de esta comprobación; en siguientes arranques retirar las cuatro variables. Preservar JWT_SECRET para conservar sesiones.
6. Importar postman/TallerTech.postman_collection.json. Definir baseUrl=http://localhost:8080, authEmail y authPassword en un entorno local privado (sin compartir/exportar credenciales). Ejecutar auth/POST Login: esperar 200. El script guarda token en variable de colección; contraseña incorrecta debe dar 401 genérico.
7. Ejecutar auth/GET Me: esperar 200 con datos públicos y sin password/passwordHash.
8. En GET /api/clients seleccionar No Auth: esperar 401 JSON. No sobrescribir el mecanismo heredado en toda la colección.
9. Restaurar Inherit auth from parent y repetir: esperar 200. Cambiar token por texto inválido: 401. Restaurar haciendo login. Probar los recursos existentes con Bearer; no se alteraron sus reglas de negocio.
10. Iniciar Android Emulator en Android Studio. Mantener Spring Boot activo en la PC; ejecutar la variante debug. Confirmar API_BASE_URL=10.0.2.2 y puerto 8080 accesible desde el emulador.
11. Iniciar sesión con la cuenta bootstrap: esperar MainActivity; probar password incorrecta y servidor detenido. Durante la solicitud el botón debe impedir dobles envíos. Girar el dispositivo durante login para comprobar continuidad.
12. Cerrar/quitar la app y volver a abrirla con backend activo: se solicita /api/auth/me. No debe pedir contraseña mientras el token sea válido.
13. Comprobar sesión persistente y caducidad: reiniciar backend conservando JWT_SECRET. Para expiración usar un tiempo breve antes de emitir un nuevo token, esperar y reabrir: exigir login. Con token vigente y backend inaccesible, reabrir permite Room con aviso offline; sin token no hay login simulado. Validar que los CRUD Room y datos previos siguen disponibles y no generan llamadas de sincronización.
14. En Profile pulsar Cerrar sesión: regresar a Login; Atrás/cerrar/reabrir no debe recuperar la sesión. Los registros locales Room deben conservarse.

## Verificación y límites

Las pruebas automatizadas usan H2 aislado y MockMvc con la cadena de seguridad real. Las pruebas de Etapa 1 usan un usuario simulado para conservar sus verificaciones de negocio; las de autenticación realizan login real BCrypt y solicitudes Bearer.
Incluyen login correcto/incorrecto, acceso sin token/con token, token inválido/expirado, /me, ausencia de hash en JSON, bootstrap idempotente y configuración parcial, usuario eliminado/antiguo y serialización de errores 403.
La prueba 403 verifica el formato del manejador; no se introduce una política por roles para fabricar una denegación.
Compilar no equivale a haber probado MariaDB, Postman, Android Keystore o interacción real del emulador.

Sin sincronización Room/API, WorkManager, refresh tokens, OAuth, registro público de cuentas, deployment cloud ni cambios de esquema Room.
No se añade administración de contraseñas, revocación por token, rate limiting ni HTTPS productivo en esta etapa.
No usar HTTP local ni la cuenta bootstrap como configuración productiva.

Resultado automatizado final de esta implementación:
- Java 21, `.\mvnw.cmd clean package`: BUILD SUCCESS; 32 tests, failures 0, errors 0, skipped 0 (13 seguridad + 19 regresión).
- `.\gradlew.bat assembleDebug`: BUILD SUCCESSFUL in 2s; 35 actionable tasks: 4 executed, 31 up-to-date.
- Advertencias Maven: auto-attachment Mockito/Byte Buddy y class sharing del JDK; los errores SQL de unicidad/FK son los casos negativos esperados de pruebas existentes.
- La primera compilación Android notificó uso de APIs obsoletas; no impidió el build.
- No se ejecutaron pruebas de interacción del emulador: adb no está disponible en la ruta platform-tools del SDK configurado. No se instaló software global.
- `git diff --check` sin errores; sin cambios en entidades/DAOs/base Room ni en los cinco repositorios operativos.
