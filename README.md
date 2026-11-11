# TallerTech

TallerTech es una aplicación móvil Android para la gestión de talleres técnicos. Permite administrar clientes, equipos, órdenes de servicio, diagnósticos, reparaciones, usuarios e historial de atención.

El proyecto utiliza una arquitectura **offline-first**, permitiendo trabajar con datos locales mediante Room y sincronizarlos con un backend REST desarrollado en Spring Boot cuando existe conexión a Internet.

---

## Arquitectura general

TallerTech está dividido en dos componentes principales:

```text
TallerTech/
│
├── app/
│   └── Aplicación móvil Android
│
└── backend/
    └── API REST desarrollada con Spring Boot
```

El flujo general de datos es:

```text
Android
   ↓
Room
   ↓
Repositories
   ↓
WorkManager
   ↓
Retrofit
   ↓
HTTPS
   ↓
Nginx
   ↓
Spring Boot
   ↓
MariaDB
```

---

# Tecnologías utilizadas

## Aplicación Android

- Java
- XML Views
- Android Studio
- Material Components
- Room
- Retrofit
- Gson
- OkHttp
- WorkManager
- Android Keystore
- Gradle Kotlin DSL
- Adaptive Icons
- Splash Screen API

---

## Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Hibernate
- Bean Validation
- JWT
- BCrypt
- Maven

---

## Base de datos

### Local

- Room
- SQLite

### Remota

- MariaDB

---

## Infraestructura

El backend está desplegado sobre infraestructura de Amazon Web Services.

Tecnologías utilizadas:

- Amazon EC2
- Ubuntu Server
- Nginx
- systemd
- Elastic IP
- HTTPS
- Let's Encrypt
- Certbot

---

# Módulos principales

## Autenticación

Gestiona:

- Inicio de sesión
- Sesiones
- JWT
- Roles
- Cierre de sesión

---

## Clientes

Permite:

- Registrar clientes
- Consultar clientes
- Buscar clientes
- Editar información
- Consultar detalle

---

## Equipos

Permite:

- Registrar equipos
- Asociar equipos a clientes
- Consultar equipos
- Buscar equipos
- Editar información

---

## Órdenes de servicio

Permite:

- Crear órdenes
- Asociar cliente y equipo
- Registrar problema reportado
- Asignar técnico
- Gestionar estados
- Consultar detalle de la orden

---

## Diagnósticos

Permite registrar y consultar el diagnóstico asociado a una orden de servicio.

---

## Reparaciones

Permite registrar y consultar la información de reparación asociada a una orden.

---

## Historial

Muestra el historial de servicios utilizando la información almacenada en las órdenes, diagnósticos y reparaciones.

---

## Usuarios

Permite administrar usuarios y roles dentro del sistema.

---

## Sincronización

Gestiona la comunicación entre la base de datos local y el backend remoto.

Cada registro puede utilizar uno de los siguientes estados:

```text
PENDING
SYNCED
ERROR
```

La sincronización se realiza automáticamente mediante WorkManager cuando existe conexión.

---

# Arquitectura offline-first

Room funciona como la fuente principal de información para la interfaz de usuario.

Cuando se crea o modifica un registro:

```text
Usuario
   ↓
Room
   ↓
Registro PENDING
   ↓
WorkManager
   ↓
Retrofit
   ↓
API REST
   ↓
MariaDB
```

De esta forma la aplicación puede seguir funcionando aunque temporalmente no exista conexión a Internet.

---

# Sincronización de entidades

Para conservar correctamente las relaciones entre los datos, la sincronización utiliza el siguiente orden:

```text
Clientes
   ↓
Equipos
   ↓
Órdenes
   ↓
Diagnósticos
   ↓
Reparaciones
```

Los registros utilizan UUID para conservar el mismo identificador tanto en Android como en el backend.

---

# Resolución de conflictos

La sincronización utiliza una estrategia **Last Write Wins**.

Cada registro mantiene una marca de tiempo:

```text
updatedAt
```

Cuando existen dos versiones del mismo registro, se conserva la versión más reciente.

---

# Estructura Android

La aplicación Android está organizada principalmente en:

```text
app/src/main/java/com/tallertech/app/
│
├── data/
│   │
│   ├── local/
│   │   ├── dao/
│   │   └── entities/
│   │
│   ├── remote/
│   │   ├── api/
│   │   └── dto/
│   │
│   ├── repository/
│   │
│   └── sync/
│
└── ui/
    ├── clients/
    ├── equipment/
    ├── orders/
    ├── history/
    ├── users/
    ├── sync/
    └── profile/
```

### `data/local`

Contiene:

- Base de datos Room
- Entidades locales
- DAO

### `data/remote`

Contiene:

- Retrofit
- Endpoints REST
- DTO
- Configuración de comunicación con el backend

### `data/repository`

Funciona como intermediario entre la interfaz, Room y los servicios remotos.

### `data/sync`

Contiene la lógica relacionada con:

- WorkManager
- Sincronización
- Estados de sincronización
- Resolución de conflictos

### `ui`

Contiene las pantallas y componentes visuales de la aplicación.

---

# Estructura Backend

El backend está organizado en capas:

```text
backend/src/main/java/com/tallertech/backend/
│
├── controller/
├── dto/
├── entity/
├── repository/
├── security/
├── service/
└── exception/
```

### `controller`

Expone los endpoints REST.

### `dto`

Contiene los objetos utilizados para enviar y recibir información mediante la API.

### `entity`

Contiene las entidades JPA utilizadas por MariaDB.

### `repository`

Gestiona el acceso a los datos mediante Spring Data JPA.

### `service`

Contiene la lógica de negocio.

### `security`

Gestiona:

- Spring Security
- JWT
- Autenticación
- Autorización

### `exception`

Centraliza el manejo de errores del backend.

---

# Entidades principales

El sistema utiliza las siguientes entidades:

```text
User
Client
Equipment
ServiceOrder
Diagnosis
Repair
```

Las relaciones principales son:

```text
Client
   ↓
Equipment
   ↓
ServiceOrder
   ├── Diagnosis
   └── Repair
```

---

# Seguridad

El proyecto utiliza:

- Spring Security
- JWT
- BCrypt
- HTTPS
- Android Keystore
- AES-256/GCM

El backend y la base de datos no están expuestos directamente al exterior.

Nginx funciona como reverse proxy entre Internet y Spring Boot.

```text
Internet
   ↓
HTTPS
   ↓
Nginx
   ↓
Spring Boot
   ↓
MariaDB
```

---

# Configuración

El backend utiliza variables de entorno para configurar credenciales y servicios.

Variables principales:

```text
DB_URL
DB_USERNAME
DB_PASSWORD

JWT_SECRET

BOOTSTRAP_ADMIN_NAME
BOOTSTRAP_ADMIN_EMAIL
BOOTSTRAP_ADMIN_PASSWORD
BOOTSTRAP_ADMIN_ROLE

SERVER_ADDRESS
SERVER_PORT
```

Las credenciales reales no deben almacenarse directamente en el repositorio.

---

# Compilación Android

Desde la raíz del proyecto:

```powershell
.\gradlew.bat clean assembleDebug
```

El APK generado se encuentra en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

# Compilación Backend

Desde la carpeta:

```text
backend/
```

Ejecutar:

```powershell
.\mvnw.cmd clean package
```

El archivo JAR generado se encuentra dentro de:

```text
backend/target/
```

---

# Interfaz

TallerTech utiliza una interfaz basada en Material Design con:

- Navegación inferior
- Tarjetas
- Chips de estado
- Iconografía personalizada
- Splash Screen
- Adaptive Icon
- Paleta azul marino y lila

Las principales pantallas son:

```text
Login
Inicio
Clientes
Equipos
Órdenes
Perfil
Historial
Usuarios
Sincronización
```

---

# Despliegue

La arquitectura productiva está compuesta por:

```text
Android
   ↓
HTTPS
   ↓
AWS EC2
   ↓
Nginx
   ↓
Spring Boot
   ↓
MariaDB
```

Spring Boot se ejecuta como servicio de Linux mediante `systemd`.

---

# Autor

Proyecto académico desarrollado para la asignatura:

**Desarrollo de Aplicaciones Móviles II**