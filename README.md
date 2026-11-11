TallerTech

Aplicación móvil empresarial para gestión de talleres técnicos

==============================================================



1\. DESCRIPCIÓN GENERAL

\----------------------

TallerTech es una aplicación móvil Android orientada a la gestión de talleres técnicos y servicios de mantenimiento.



Permite administrar clientes, equipos, órdenes de servicio, diagnósticos, reparaciones, usuarios, historial de servicios y estado de sincronización.



La aplicación utiliza un enfoque offline-first:

\- Los datos se guardan primero de forma local en el dispositivo.

\- Las operaciones pendientes se sincronizan automáticamente con el backend cuando existe conexión.

\- El backend está desplegado en infraestructura AWS.

\- La aplicación consume la API productiva mediante HTTPS.



2\. ARQUITECTURA GENERAL

\-----------------------

Flujo principal:



Android

&#x20; -> Room

&#x20; -> Repositories

&#x20; -> WorkManager / motor de sincronización

&#x20; -> Retrofit + HTTPS

&#x20; -> Nginx

&#x20; -> Spring Boot

&#x20; -> MariaDB



Arquitectura resumida:



\[Aplicación Android]

&#x20;   |

&#x20;   | HTTPS

&#x20;   v

\[AWS EC2 - Nginx]

&#x20;   |

&#x20;   | localhost:8080

&#x20;   v

\[Spring Boot REST API]

&#x20;   |

&#x20;   | localhost:3306

&#x20;   v

\[MariaDB]



3\. TECNOLOGÍAS UTILIZADAS

\-------------------------



ANDROID

\-------

\- Android Studio

\- Java

\- XML Views

\- Material Components

\- Room 2.8.5

\- Retrofit 3.0.0

\- Gson Converter 3.0.0

\- OkHttp 4.12.0

\- WorkManager

\- Android Keystore

\- AES-256/GCM para almacenamiento seguro del token

\- Adaptive Icons

\- Splash Screen nativa

\- Gradle Kotlin DSL



BACKEND

\-------

\- Java 21

\- Spring Boot 3.5.7

\- Spring Web

\- Spring Data JPA

\- Spring Security

\- Hibernate

\- Bean Validation

\- JJWT 0.13.0

\- Maven

\- API REST

\- BCrypt para contraseñas

\- JWT para autenticación



BASE DE DATOS

\-------------

\- MariaDB 10.11

\- Base de datos remota desplegada dentro de la instancia EC2

\- Acceso restringido a localhost

\- Relaciones entre clientes, equipos, órdenes, diagnósticos y reparaciones



INFRAESTRUCTURA / CLOUD

\-----------------------

\- Amazon Web Services (AWS)

\- Amazon EC2

\- Ubuntu Server 24.04 LTS

\- Tipo de instancia usado: t3.micro

\- Nginx como reverse proxy

\- systemd para ejecutar Spring Boot como servicio

\- Elastic IP para mantener dirección pública estable

\- HTTPS mediante Let's Encrypt + Certbot

\- Renovación automática del certificado

\- Puerto 22 SSH restringido al administrador

\- Puerto 80 HTTP redirigido a HTTPS

\- Puerto 443 HTTPS público

\- MariaDB 3306 no expuesto públicamente

\- Spring Boot 8080 no expuesto públicamente



4\. URL PRODUCTIVA

\-----------------

API productiva:



https://18.207.16.220/



La aplicación Android utiliza esta URL para comunicarse con el backend desplegado en AWS.



5\. FUNCIONALIDADES PRINCIPALES

\------------------------------



AUTENTICACIÓN

\-------------

\- Login mediante correo y contraseña

\- JWT

\- Persistencia segura del token

\- Validación de sesión

\- Logout

\- Roles de usuario



CLIENTES

\--------

\- Registrar clientes

\- Consultar clientes

\- Buscar clientes

\- Editar información

\- Ver detalle



EQUIPOS

\-------

\- Registrar equipos asociados a clientes

\- Consultar equipos

\- Buscar equipos

\- Editar información

\- Ver detalle



ÓRDENES DE SERVICIO

\-------------------

\- Crear órdenes

\- Asociar cliente y equipo

\- Registrar problema reportado

\- Cambiar estado

\- Asignar técnico

\- Consultar detalle

\- Buscar y filtrar órdenes



DIAGNÓSTICOS

\------------

\- Registrar diagnóstico de una orden

\- Consultar diagnóstico

\- Relación uno a uno con la orden



REPARACIONES

\------------

\- Registrar información de reparación

\- Consultar reparación

\- Relación uno a uno con la orden



HISTORIAL

\---------

\- Historial derivado de los datos existentes

\- Muestra orden, cliente, equipo, técnico, fecha, diagnóstico y reparación

\- No utiliza una tabla de historial separada



USUARIOS

\--------

\- Gestión de usuarios

\- Creación y edición

\- Roles

\- Administración restringida según permisos



SINCRONIZACIÓN

\--------------

\- Estado de sincronización

\- Registros pendientes

\- Registros con error

\- Última sincronización exitosa

\- Sincronización manual

\- Sincronización automática



6\. FUNCIONAMIENTO OFFLINE-FIRST

\-------------------------------

Room es la fuente principal de datos para la interfaz.



Estados de sincronización:

\- PENDING

\- SYNCED

\- ERROR



Flujo de escritura:

1\. El usuario crea o modifica información.

2\. El cambio se guarda inmediatamente en Room.

3\. El registro se marca como PENDING.

4\. WorkManager programa la sincronización.

5\. Cuando existe conexión, el registro se envía al backend.

6\. Si la operación es correcta, pasa a SYNCED.

7\. Si falla, permanece disponible localmente y puede reintentarse.



Orden de sincronización:

1\. Clientes

2\. Equipos

3\. Órdenes

4\. Diagnósticos

5\. Reparaciones



Resolución de conflictos:

\- Last Write Wins utilizando updatedAt.

\- Se conserva la versión más reciente.



7\. SEGURIDAD

\------------

\- HTTPS/TLS para tráfico Android -> servidor

\- JWT para autenticación

\- Spring Security

\- BCrypt para almacenar contraseñas

\- Android Keystore

\- AES-256/GCM para proteger el token local

\- Base de datos no expuesta a Internet

\- Spring Boot no expuesto directamente

\- Nginx como reverse proxy

\- SSH restringido al administrador

\- Variables de entorno para credenciales del servidor

\- Separación de roles



8\. ESTRUCTURA GENERAL DEL PROYECTO

\---------------------------------

app/

&#x20; Aplicación Android



backend/

&#x20; API REST Spring Boot



Componentes Android relevantes:

\- data/local

\- data/remote

\- data/repository

\- data/sync

\- ui

\- res



Componentes Backend relevantes:

\- controller

\- dto

\- entity

\- repository

\- security

\- service

\- exception



9\. ENTIDADES PRINCIPALES

\------------------------

\- User

\- Client

\- Equipment

\- ServiceOrder

\- Diagnosis

\- Repair



Los identificadores son UUID y se mantienen iguales entre Room y el backend remoto.



10\. DESPLIEGUE DEL BACKEND

\--------------------------

Compilación:



cd backend

mvnw.cmd clean package



El JAR se ejecuta en AWS mediante systemd.



Servicio:

tallertech.service



Spring Boot escucha localmente en:



127.0.0.1:8080



Nginx recibe solicitudes HTTPS públicas y las reenvía hacia Spring Boot.



11\. CONFIGURACIÓN DE BASE DE DATOS

\----------------------------------

Variables utilizadas por el backend:



DB\_URL

DB\_USERNAME

DB\_PASSWORD



Ejemplo:



DB\_URL=jdbc:mariadb://127.0.0.1:3306/tallertech

DB\_USERNAME=tallertech\_app

DB\_PASSWORD=<configurar\_en\_servidor>



No se recomienda guardar contraseñas reales en GitHub.



12\. CONFIGURACIÓN JWT

\---------------------

Variables principales:



JWT\_SECRET

BOOTSTRAP\_ADMIN\_NAME

BOOTSTRAP\_ADMIN\_EMAIL

BOOTSTRAP\_ADMIN\_PASSWORD

BOOTSTRAP\_ADMIN\_ROLE



El secreto JWT y las contraseñas deben configurarse mediante variables de entorno.



13\. EJECUCIÓN ANDROID

\---------------------

Desde la raíz del proyecto:



gradlew.bat clean assembleDebug



APK debug:



app/build/outputs/apk/debug/app-debug.apk



La aplicación requiere Internet para sincronización remota, pero mantiene funcionamiento local sin conexión.



14\. PRUEBAS REALIZADAS

\----------------------

\- Compilación Android

\- Pruebas unitarias

\- Pruebas instrumentadas

\- Login contra backend AWS

\- Sincronización Android -> AWS

\- Creación de cliente

\- Creación de equipo

\- Creación de orden

\- Persistencia real en MariaDB

\- Flujo offline -> online

\- HTTPS

\- Redirección HTTP -> HTTPS



15\. VALIDACIÓN REAL DE SINCRONIZACIÓN

\------------------------------------

Se verificó una prueba completa:



Android

&#x20; -> Room

&#x20; -> WorkManager

&#x20; -> HTTPS

&#x20; -> AWS EC2

&#x20; -> Nginx

&#x20; -> Spring Boot

&#x20; -> MariaDB



Los registros creados desde Android fueron comprobados directamente en MariaDB.



16\. UI / UX

\-----------

La interfaz utiliza:

\- Diseño claro

\- Azul marino como color principal

\- Acentos lilas

\- Tarjetas

\- Bordes suaves

\- Material Design

\- Perfil rediseñado

\- Pantalla de sincronización

\- Historial

\- Gestión de usuarios

\- Splash Screen

\- Icono propio de TallerTech



17\. CONTROL DE VERSIONES

\------------------------

Se utiliza Git + GitHub.



Ramas de desarrollo usadas durante el proyecto:

\- main

\- codex/offline-sync

\- codex/aws-production



Checkpoints importantes:

\- checkpoint-room-clientes

\- checkpoint-room-completo

\- checkpoint-backend-etapa1

\- checkpoint-auth-retrofit

\- checkpoint-offline-sync

\- checkpoint-aws-production



18\. REQUISITOS ACADÉMICOS CUBIERTOS

\-----------------------------------

\- Aplicación móvil empresarial

\- UI/UX

\- Datos locales y remotos

\- Sincronización

\- DAO

\- Web Services REST

\- Backend desplegado en infraestructura IaaS

\- AWS

\- Seguridad

\- HTTPS

\- Autenticación

\- Persistencia remota

\- Operación offline

\- Preparado para despliegue y validación en dispositivo físico



19\. ESTADO ACTUAL

\-----------------

\- Frontend Android funcional

\- Persistencia Room

\- Backend Spring Boot

\- MariaDB

\- Autenticación JWT

\- Sincronización offline-first

\- WorkManager

\- AWS EC2

\- Nginx

\- HTTPS

\- Elastic IP

\- UI/UX mejorada

\- Splash Screen

\- Icono propio

\- Perfil

\- Historial

\- Usuarios

\- Sincronización

\- Integración Android -> AWS -> MariaDB validada



20\. NOTAS

\---------

\- No publicar secretos o contraseñas reales en GitHub.

\- Mantener el certificado HTTPS con renovación automática.

\- Mantener activo tallertech.service.

\- Mantener activo Nginx.

\- Mantener activa MariaDB.

\- Antes de la entrega final, validar en dispositivo físico.

