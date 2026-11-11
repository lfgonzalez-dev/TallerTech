\# TallerTech



Aplicación móvil Android para la gestión integral de talleres técnicos, desarrollada con arquitectura \*\*offline-first\*\*, persistencia local mediante \*\*Room\*\*, sincronización automática con una \*\*API REST en Spring Boot\*\* y despliegue productivo sobre \*\*Amazon Web Services (AWS)\*\*.



TallerTech permite administrar clientes, equipos, órdenes de servicio, diagnósticos, reparaciones, usuarios y el historial de atención desde una única aplicación móvil, manteniendo disponibilidad de los datos incluso cuando el dispositivo pierde conexión a Internet.



\---



\## Descripción



TallerTech fue desarrollado como una solución móvil empresarial para digitalizar el flujo operativo de un taller técnico.



La aplicación permite que los técnicos y administradores puedan trabajar desde un dispositivo Android mientras la información se mantiene disponible tanto de forma local como remota.



El sistema implementa un modelo \*\*offline-first\*\*:



1\. Las operaciones se guardan primero en la base de datos local del dispositivo.

2\. Los registros pendientes se identifican mediante estados de sincronización.

3\. WorkManager ejecuta la sincronización cuando existe conectividad.

4\. Retrofit envía los cambios mediante HTTPS al backend.

5\. Spring Boot procesa las operaciones y las persiste en MariaDB.

6\. Los datos remotos se integran nuevamente con Room.



Esto permite continuar trabajando incluso durante interrupciones temporales de conectividad.



\---



\# Arquitectura



```text

┌───────────────────────────────┐

│        Android App            │

│                               │

│ Java + XML + Material Design  │

└───────────────┬───────────────┘

&#x20;               │

&#x20;               ▼

┌───────────────────────────────┐

│             Room              │

│     Base de datos local       │

└───────────────┬───────────────┘

&#x20;               │

&#x20;               ▼

┌───────────────────────────────┐

│         Repositories          │

│                               │

│ Fuente principal para la UI   │

└───────────────┬───────────────┘

&#x20;               │

&#x20;               ▼

┌───────────────────────────────┐

│          WorkManager          │

│      Motor de sincronización  │

└───────────────┬───────────────┘

&#x20;               │

&#x20;             HTTPS

&#x20;               │

&#x20;               ▼

┌───────────────────────────────┐

│          AWS EC2              │

│                               │

│             Nginx             │

└───────────────┬───────────────┘

&#x20;               │

&#x20;         127.0.0.1:8080

&#x20;               │

&#x20;               ▼

┌───────────────────────────────┐

│       Spring Boot API         │

│                               │

│ JWT + Spring Security + JPA   │

└───────────────┬───────────────┘

&#x20;               │

&#x20;         127.0.0.1:3306

&#x20;               │

&#x20;               ▼

┌───────────────────────────────┐

│           MariaDB             │

│      Base de datos remota     │

└───────────────────────────────┘

```



\---



\# Tecnologías utilizadas



\## Android



\- Java

\- XML Views

\- Material Components

\- Android Studio

\- Gradle Kotlin DSL

\- Room 2.8.5

\- Retrofit 3.0.0

\- Gson Converter 3.0.0

\- OkHttp 4.12.0

\- WorkManager

\- Android Keystore

\- AES-256/GCM

\- Adaptive Icons

\- Splash Screen API



\## Backend



\- Java 21

\- Spring Boot 3.5.7

\- Spring Web

\- Spring Data JPA

\- Spring Security

\- Hibernate

\- Bean Validation

\- JJWT 0.13.0

\- BCrypt

\- Maven

\- REST API



\## Base de datos



\- MariaDB 10.11

\- Room / SQLite en Android



\## Infraestructura



\- Amazon Web Services

\- Amazon EC2

\- Ubuntu Server 24.04 LTS

\- Nginx

\- systemd

\- Elastic IP

\- Let's Encrypt

\- Certbot

\- HTTPS / TLS



\---



\# Funcionalidades principales



\## Autenticación



\- Inicio de sesión mediante correo y contraseña.

\- Autenticación mediante JWT.

\- Validación de sesión existente.

\- Persistencia segura del token.

\- Cierre de sesión.

\- Control de acceso por roles.



\---



\## Clientes



\- Registrar clientes.

\- Consultar listado.

\- Buscar por nombre, teléfono o correo.

\- Editar información.

\- Visualizar detalle del cliente.



\---



\## Equipos



\- Registrar equipos.

\- Asociar equipos a clientes.

\- Buscar equipos.

\- Editar información.

\- Consultar estado.

\- Visualizar detalle del equipo.



\---



\## Órdenes de servicio



\- Crear órdenes.

\- Relacionar cliente y equipo.

\- Registrar problema reportado.

\- Asignar técnico.

\- Cambiar estado.

\- Buscar y filtrar órdenes.

\- Consultar detalle completo.



\---



\## Diagnósticos



\- Registrar diagnóstico de una orden.

\- Consultar diagnóstico existente.

\- Mantener relación directa con la orden de servicio.



\---



\## Reparaciones



\- Registrar información de reparación.

\- Consultar reparación.

\- Asociar la reparación con una orden.



\---



\## Historial



La pantalla de historial se genera a partir de la información real almacenada en Room.



Permite visualizar:



\- Código de orden.

\- Cliente.

\- Equipo.

\- Fecha.

\- Técnico.

\- Estado.

\- Diagnóstico.

\- Reparación.



No utiliza una tabla de historial adicional.



\---



\## Usuarios



Los administradores pueden:



\- Consultar usuarios.

\- Crear usuarios.

\- Editar información.

\- Asignar roles.

\- Administrar acceso al sistema.



La contraseña nunca se almacena dentro de Room.



\---



\## Sincronización



La aplicación incluye una pantalla dedicada para consultar:



\- Estado general.

\- Conectividad.

\- Registros pendientes.

\- Registros con error.

\- Última sincronización exitosa.

\- Ejecución manual de sincronización.



\---



\# Arquitectura offline-first



Room actúa como fuente principal de datos para la interfaz.



Cada registro utiliza un estado de sincronización:



```text

PENDING

SYNCED

ERROR

```



\## Flujo de escritura



```text

Usuario realiza una operación

&#x20;           ↓

Se guarda inmediatamente en Room

&#x20;           ↓

Estado = PENDING

&#x20;           ↓

WorkManager programa sincronización

&#x20;           ↓

Existe conexión a Internet

&#x20;           ↓

Retrofit envía el registro al servidor

&#x20;           ↓

Spring Boot procesa la operación

&#x20;           ↓

MariaDB guarda la información

&#x20;           ↓

Estado local = SYNCED

```



Si la operación falla, los datos permanecen disponibles localmente.



\---



\# Orden de sincronización



Para mantener las relaciones entre entidades se utiliza el siguiente orden:



```text

1\. Clientes

2\. Equipos

3\. Órdenes

4\. Diagnósticos

5\. Reparaciones

```



\---



\# Resolución de conflictos



TallerTech utiliza una estrategia:



```text

Last Write Wins

```



Cada entidad mantiene un campo:



```text

updatedAt

```



Cuando existen dos versiones de un mismo registro, se conserva la versión más reciente.



Los UUID se mantienen iguales entre Android y backend.



\---



\# Seguridad



TallerTech incorpora diferentes mecanismos de seguridad.



\## Comunicación



Todo el tráfico productivo Android → servidor utiliza:



```text

HTTPS / TLS

```



\## Autenticación



El backend utiliza:



```text

Spring Security

JWT

BCrypt

```



\## Android



Los tokens se almacenan utilizando:



```text

Android Keystore

AES-256/GCM

```



\## Infraestructura



La arquitectura productiva evita exponer servicios internos innecesariamente.



```text

22    SSH         Acceso administrativo restringido

80    HTTP        Redirección a HTTPS

443   HTTPS       API pública

8080  Spring Boot Solo localhost

3306  MariaDB     Solo localhost

```



MariaDB y Spring Boot no están expuestos directamente a Internet.



\---



\# Infraestructura AWS



El backend está desplegado sobre una instancia Amazon EC2.



```text

Sistema operativo:

Ubuntu Server 24.04 LTS



Servidor HTTP:

Nginx



Backend:

Spring Boot



Base de datos:

MariaDB



Administrador de servicios:

systemd

```



Nginx funciona como reverse proxy:



```text

Internet

&#x20;  ↓

HTTPS :443

&#x20;  ↓

Nginx

&#x20;  ↓

127.0.0.1:8080

&#x20;  ↓

Spring Boot

&#x20;  ↓

127.0.0.1:3306

&#x20;  ↓

MariaDB

```



\---



\# API productiva



```text

https://18.207.16.220/

```



La dirección corresponde a una Elastic IP asociada a la instancia EC2 del proyecto.



HTTP redirige automáticamente hacia HTTPS.



\---



\# Certificado HTTPS



El servidor utiliza certificados TLS emitidos mediante Let's Encrypt.



La renovación se administra automáticamente con Certbot.



Después de cada renovación, Nginx recarga automáticamente el certificado.



\---



\# Servicio Spring Boot



El backend se ejecuta mediante systemd:



```text

tallertech.service

```



Esto permite:



\- Inicio automático después de reiniciar EC2.

\- Reinicio ante fallos.

\- Administración mediante systemctl.

\- Registro mediante journalctl.



Ejemplos:



```bash

sudo systemctl status tallertech

```



```bash

sudo systemctl restart tallertech

```



```bash

sudo journalctl -u tallertech

```



\---



\# Variables de entorno



El backend utiliza variables de entorno para evitar almacenar credenciales directamente en el código fuente.



Variables principales:



```text

DB\_URL

DB\_USERNAME

DB\_PASSWORD



JWT\_SECRET



BOOTSTRAP\_ADMIN\_NAME

BOOTSTRAP\_ADMIN\_EMAIL

BOOTSTRAP\_ADMIN\_PASSWORD

BOOTSTRAP\_ADMIN\_ROLE



SERVER\_ADDRESS

SERVER\_PORT

```



Ejemplo:



```text

DB\_URL=jdbc:mariadb://127.0.0.1:3306/tallertech

DB\_USERNAME=tallertech\_app

DB\_PASSWORD=<password>

```



No deben publicarse secretos reales en el repositorio.



\---



\# Estructura del proyecto



```text

TallerTech/

│

├── app/

│   └── Aplicación Android

│

├── backend/

│   └── API REST Spring Boot

│

├── gradle/

│

├── README.md

│

├── build.gradle.kts

│

└── settings.gradle.kts

```



\---



\# Estructura Android



```text

app/src/main/java/com/tallertech/app/

│

├── data/

│   ├── local/

│   ├── remote/

│   ├── repository/

│   └── sync/

│

└── ui/

&#x20;   ├── clients/

&#x20;   ├── equipment/

&#x20;   ├── orders/

&#x20;   ├── history/

&#x20;   ├── users/

&#x20;   ├── sync/

&#x20;   └── profile/

```



\---



\# Estructura Backend



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



\---



\# Entidades principales



```text

User

Client

Equipment

ServiceOrder

Diagnosis

Repair

```



Las relaciones principales permiten mantener:



```text

Cliente

&#x20;  ↓

Equipo

&#x20;  ↓

Orden

&#x20;  ↓

Diagnóstico / Reparación

```



\---



\# Compilación Android



Desde la raíz del proyecto:



```powershell

.\\gradlew.bat clean assembleDebug

```



El APK se genera en:



```text

app/build/outputs/apk/debug/app-debug.apk

```



\---



\# Compilación Backend



Desde:



```text

TallerTech/backend

```



Ejecutar:



```powershell

.\\mvnw.cmd clean package

```



El JAR generado se encuentra dentro de:



```text

backend/target/

```



\---



\# Pruebas



Durante el desarrollo se ejecutaron:



\- Pruebas unitarias Android.

\- Pruebas instrumentadas.

\- Pruebas backend.

\- Pruebas de autenticación.

\- Pruebas de autorización.

\- Pruebas de API.

\- Pruebas offline.

\- Pruebas de sincronización.

\- Pruebas sobre MariaDB real.

\- Pruebas sobre infraestructura AWS.



\---



\# Validación real de sincronización



Se realizó una prueba completa creando desde Android:



```text

Cliente

↓

Equipo

↓

Orden de servicio

```



Los datos fueron:



1\. Guardados localmente mediante Room.

2\. Marcados para sincronización.

3\. Procesados por WorkManager.

4\. Enviados mediante HTTPS.

5\. Recibidos por Nginx.

6\. Procesados por Spring Boot.

7\. Persistidos en MariaDB.



Posteriormente se verificaron directamente mediante consultas SQL en MariaDB.



Flujo validado:



```text

Android

&#x20;  ↓

Room

&#x20;  ↓

WorkManager

&#x20;  ↓

Retrofit

&#x20;  ↓

HTTPS

&#x20;  ↓

AWS EC2

&#x20;  ↓

Nginx

&#x20;  ↓

Spring Boot

&#x20;  ↓

MariaDB

```



\---



\# UI / UX



TallerTech utiliza una identidad visual basada en:



\- Azul marino.

\- Acentos lilas.

\- Fondos claros.

\- Material Cards.

\- Chips de estado.

\- Iconografía personalizada.

\- Navegación inferior.

\- Splash Screen.

\- Adaptive Icon.



Las principales pantallas incluyen:



```text

Login

Inicio

Clientes

Equipos

Órdenes

Perfil

Sincronización

Historial

Usuarios

```



\---



\# Control de versiones



El proyecto utiliza:



```text

Git

GitHub

```



Durante el desarrollo se utilizaron ramas independientes para implementar funcionalidades de forma segura.



Ejemplos:



```text

main

codex/offline-sync

codex/aws-production

```



\---



\# Checkpoints



El repositorio mantiene tags importantes del desarrollo:



```text

checkpoint-room-clientes

checkpoint-room-completo

checkpoint-backend-etapa1

checkpoint-auth-retrofit

checkpoint-offline-sync

checkpoint-aws-production

checkpoint-final-tallertech

```



Esto permite recuperar versiones estables del proyecto.



\---



\# Estado actual



Actualmente TallerTech cuenta con:



\- Aplicación Android funcional.

\- Persistencia local Room.

\- Backend Spring Boot.

\- MariaDB.

\- Autenticación JWT.

\- Spring Security.

\- Sincronización offline-first.

\- WorkManager.

\- Retrofit.

\- AWS EC2.

\- Nginx.

\- HTTPS.

\- Elastic IP.

\- Perfil de usuario.

\- Gestión de usuarios.

\- Historial.

\- Pantalla de sincronización.

\- Splash Screen.

\- Icono personalizado.

\- UI/UX consistente.

\- Integración Android → AWS → MariaDB validada.



\---



\# Requisitos académicos cubiertos



El proyecto implementa los principales componentes solicitados para una aplicación móvil empresarial:



\- Persistencia local.

\- Persistencia remota.

\- DAO.

\- Web Services REST.

\- Sincronización de datos.

\- Operación offline.

\- Seguridad.

\- Autenticación.

\- Roles.

\- UI/UX.

\- Backend desplegado en infraestructura IaaS.

\- AWS.

\- HTTPS.

\- Preparación para ejecución en dispositivo Android físico.



\---



\# Autor



\*\*TallerTech\*\*  

Proyecto académico desarrollado para la asignatura \*\*Desarrollo de Aplicaciones Móviles II\*\*.



\---



\# Licencia



Proyecto desarrollado con fines académicos y educativos.

