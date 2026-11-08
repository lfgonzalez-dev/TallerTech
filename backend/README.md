# TallerTech Backend — Etapa 1

Proyecto independiente de Android: Java 21, Spring Boot 3.5.7, Maven 3.9.9, Spring Web, Spring Data JPA, MariaDB y Bean Validation. No requiere cambios en Room ni en Gradle.

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

No se incluye `syncStatus`: es exclusivamente local. User no contiene password en Room; no se acepta ni almacena password en esta etapa. Los campos JSON desconocidos se rechazan con 400.

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

Importar `postman/TallerTech.postman_collection.json` mediante Import en Postman. Configurar la variable de colección `baseUrl`. Ejecutar las carpetas en este orden: clients, equipment, orders, diagnoses, repairs; users es independiente. Los POST guardan los IDs en variables. Los ejemplos usan IDs estables y datos ficticios; repetir un POST debe producir 409. Para nuevas pruebas cambiar ID y campos únicos, sin borrar bases existentes.

1. Arrancar contra MariaDB y confirmar que Hibernate crea seis tablas, FK e índices únicos.
2. Crear cliente, equipo, orden, diagnóstico, reparación y usuario.
3. Consultar listas, IDs y filtros, y actualizar mediante PUT.
4. Verificar preservación exacta de UUID y updatedAt en POST; avance de updatedAt en PUT.
5. Repetir serial/code/email y diagnóstico/reparación de una misma orden: 409.
6. Usar padre inexistente: 404; usar equipo de otro cliente: 409.
7. Probar validación de email, UUID, estado, campos requeridos y JSON mal formado: 400.
8. Reiniciar Spring Boot y confirmar persistencia de los datos en MariaDB.
9. Revisar restricciones con acceso SQL local y probar escrituras concurrentes de códigos/seriales únicos.

## Alcance y límites

Sin DELETE, paginación, generación automática de código visible, migraciones versionadas ni autenticación. El cliente suministra code separado del UUID; Android ya genera códigos propios. Sin passwords ni login. Esta API de desarrollo permite operaciones sin autenticación y debe ejecutarse localmente en esta etapa.

Reservados para Etapa 2 o posteriores: autenticación, JWT/Spring Security y manejo seguro de credenciales. Retrofit, WorkManager, sincronización Room ↔ API, resolución de conflictos y despliegue cloud quedan fuera de esta implementación. Android permanece independiente.

## Validación realizada en este entorno

Se ejecutaron las pruebas MockMvc/JPA con H2 y el build Android. El intento de iniciar el JAR contra MariaDB local, con los valores predeterminados y ddl-auto=validate (sin alterar esquema), falló por autenticación GSS-API, SQLState 28000 / error 1045. No se utilizaron credenciales personales ni se modificó la base. La persistencia real en MariaDB y las pruebas interactivas de Postman quedan pendientes de configurar el usuario de desarrollo indicado arriba.

