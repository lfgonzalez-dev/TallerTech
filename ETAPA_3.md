# TallerTech — Etapa 3

Implementación revisada en la rama `codex/offline-sync`. La auditoría inicial encontró el árbol limpio.
La compilación y las pruebas automatizadas no sustituyen las pruebas de interfaz ni una integración real con MariaDB.

## 1. Auditoría inicial

| Módulo | Estado inicial verificado | Resultado de esta etapa |
| --- | --- | --- |
| Login | JWT, Retrofit, Keystore y sesión local disponibles | Conservados; caché de nombre del usuario sin contraseñas y aviso de expiración |
| Home | Estadísticas y actividad consultadas mediante repositories/Room | Recarga al invalidarse Room |
| Clients y detalle/edición | CRUD local y estadísticas existentes | Escrituras PENDING, timestamps y programación de sync; contador de servicios reales |
| Equipment y detalle/edición | Persistencia y JOIN con clientes disponibles | Integrados con sync y actualización de pantalla desde Room |
| Orders y detalle/estado | Creación, consulta y estado locales disponibles | Integrados con sync y observación de datos locales |
| Diagnosis | Consulta/guardado local por orden | Sync conservando UUID y unicidad por orden |
| Repair | Consulta/guardado local por orden | Sync conservando UUID y unicidad por orden |
| Users | CRUD REST existente, creación sin contraseña ni autorización ADMIN | Gestión autenticada ADMIN y módulo Android |
| History | No existía pantalla | JOIN local sin tabla adicional |
| Profile | Datos de sesión y cierre de sesión; gran espacio vacío | Avatar, tarjeta de información y accesos |
| Synchronization | DTO y API existentes, sin motor ni pantalla | Motor, WorkManager y pantalla real |

También se revisaron entidades, DAO, repositories, DTO, interceptores, TokenManager,
controllers, servicios, repositories JPA, seguridad, layouts, adapters, modelos y navegación.
Las búsquedas de referencias y Lint identificaron recursos sin uso y datos ficticios en XML activos.

## 2. Arquitectura de sincronización

`UI → Repository → Room ↔ SyncCoordinator/SyncEngine ↔ Retrofit ↔ Spring Boot ↔ MariaDB`.
SyncEngine centraliza comparación, merge, confirmación y tratamiento de errores.
SyncResources adapta los cinco recursos con DAO y Retrofit tipados.
Las respuestas se aplican dentro de transacciones, releyendo la versión local para conservar ediciones concurrentes.
No se vacía la base. Las inserciones operativas usan ABORT para evitar reemplazos que provoquen borrados en cascada.

## 3. WorkManager

Dependencia `androidx.work:work-runtime:2.10.1`, Worker Java, restricción CONNECTED y
trabajo único `tallertech-sync` con APPEND_OR_REPLACE: solicitudes serializadas, sin ejecución simultánea.
Se preserva una solicitud realizada mientras termina otro worker.
Backoff exponencial desde 30 segundos, con hasta cinco reintentos tras el intento inicial.
No hay polling. Tras agotar los reintentos se requiere un nuevo disparador.
Referencia: [documentación oficial sobre trabajo único](https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/manage-work).

## 4. Disparadores

MainActivity solicita sync al abrirse/reanudarse, incluyendo la entrada posterior a login válido.
Los cinco repositories operativos solicitan sync después de persistir sus escrituras.
WorkManager espera/restablece las condiciones de red. La pantalla ofrece Sincronizar ahora.

## 5. Orden

Clients → Equipment → ServiceOrders → Diagnosis → Repair.
Cada recurso descarga/combina antes de subir sus pendientes.
Un hijo local espera si sus padres aún no están SYNCED.

## 6. Estados

PENDING: versión local por confirmar. SYNCED: versión confirmada/descargada.
ERROR: rechazo de datos o integridad que requiere seguimiento.
Editar localmente vuelve a PENDING; Sincronizar ahora también reintenta ERROR.
Los estados solo existen en Android. Los conteos se consultan en Room.

## 7. Last Write Wins

Epoch milliseconds en todos los recursos. Mayor updatedAt gana.
En igualdad gana la versión almacenada en servidor; el reintento es idempotente.
El reloj local avanza al menos un milisegundo respecto de la versión persistida.
GET por UUID determina POST frente a PUT; un POST concurrente con 409 se reconcilia mediante GET.
No se cambian UUID para resolver conflictos de unicidad.
Como cualquier LWW por reloj de dispositivo, la exactitud temporal depende de los relojes de los dispositivos.

## 8. Backend updatedAt

Los cinco PUT operativos comparan bajo bloqueo pesimista transaccional.
Una versión antigua o igual devuelve la versión definitiva almacenada sin sobrescribirla.
Una nueva conserva el timestamp recibido. Para compatibilidad con clientes REST anteriores,
un PUT sin updatedAt continúa representando una edición fechada por el servidor.
Android siempre envía updatedAt.

## 9. DAO

Consultas PENDING, conteos por estado, cambio de estado condicionado por UUID+versión y reintento de ERROR.
ServiceOrderDao incorpora el JOIN de historial y conteo de servicios de los últimos 30 días.
No hay SQL en Fragments.

## 10. Repositories

Los cinco repositories operativos guardan antes de programar sync.
Client/Equipment actualizan versión y contenido en una transacción.
HistoryRepository consulta Room; UserRepository concentra la gestión remota ADMIN.
StatisticsRepository sigue consultando exclusivamente Room.
AuthRepository conserva datos públicos mínimos del usuario localmente.

## 11. Mappers

ClientMapper, EquipmentMapper, ServiceOrderMapper, DiagnosisMapper y RepairMapper:
Entity → Request y Response → Entity, preservando UUID, campos y epoch milliseconds.
Las respuestas válidas generan entidades SYNCED. No incluyen syncStatus en requests.

## 12. Offline

Consulta, creación y edición operativa siguen usando Room.
No se revierte una escritura por red caída ni se convierte una pérdida de red en ERROR de registro.
El acceso offline conserva las reglas existentes de sesión JWT válida previamente guardada.
Usuarios necesita servidor y muestra un mensaje si no puede conectarse.

## 13. Recuperación de Internet

El trabajo persistente pendiente se ejecuta cuando cumple su restricción de red.
Los fallos recuperables conservan PENDING y usan backoff.
Reanudar la app o usar Sincronizar ahora programa otro intento si se agotó el límite anterior.

## 14. HTTP y sesión

AuthInterceptor sigue añadiendo Bearer JWT y eliminándolo ante 401.
El worker detiene el intento sin retry de 401, invalida la sesión en memoria y la Activity vuelve al login.
Room no se elimina. 403 detiene por permisos; 408/429/5xx son recuperables.
400/404 de escritura/409 no reconciliable/422 marcan ERROR en la versión afectada.
Un 404 al consultar existencia significa POST. Los errores de descarga no marcan indiscriminadamente datos locales.
El contexto técnico sin secretos se registra en Logcat; la pantalla no muestra UUID internos.

## 15. Usuarios

Android permite listar, consultar por ID internamente, crear y editar nombre, correo y rol.
La creación solicita contraseña; la edición puede cambiarla opcionalmente.
La contraseña se limpia del formulario, no se guarda en estado de vistas ni en Room.
El backend utiliza BCrypt, valida longitud mínima de 12 caracteres y máximo de 72 bytes UTF-8.
Las respuestas nunca incluyen password/passwordHash. No hay registro público.

## 16. Roles

Endpoints `/api/users` y `/api/users/**` restringidos a ADMIN.
Roles nuevos ADMIN/TECNICO; se normalizan equivalentes existentes como Administrador/Técnico.
El filtro obtiene autoridades del usuario actual en la base, de modo que una revocación de ADMIN
se aplica incluso a un token emitido antes del cambio. Los demás usuarios reciben 403.

## 17. Historial

JOIN entre órdenes, clientes, equipos, diagnósticos y reparaciones.
Muestra orden, cliente, equipo, fecha, estado, técnico, diagnóstico y reparación cuando existen.
La selección abre el detalle de la orden por UUID interno.
No se creó tabla history ni se duplicaron registros.

## 18. Pantalla Sincronización

Conectividad observada, PENDING, ERROR, última ejecución completa exitosa, problema actual y estado general.
El estado considera también WorkInfo y no muestra Sincronizado si hay pendientes.
Sincronizar ahora reintenta registros ERROR y programa el trabajo persistente.

## 19. Perfil

Título, avatar circular con iniciales, nombre, correo, rol legible y tarjeta.
Accesos a Sincronización, Historial, Usuarios si ADMIN y Cerrar sesión.
No presenta token, UUID ni hash.

## 20. Navegación

Accesos desde Perfil con back stack y botones Volver.
BottomNavigation mantiene sus cinco destinos.
LocalDataObserver recarga las pantallas de consulta al invalidarse Room,
sin reemplazar contenido de formularios mientras el usuario escribe.

## 21. Datos ficticios retirados

Nombres, iniciales, contactos, conteos y equipos de ejemplo de
fragment_client_detail, fragment_equipment_detail, item_client e item_equipment.
El técnico inicial de una orden procede ahora de la sesión.
Se completó el contador real de servicios recientes que antes quedaba sin actualizar.
Las fixtures y ejemplos de tests/Postman se conservaron.

## 22. Archivos eliminados

Ninguno. No se identificó una clase/layout activo cuya eliminación fuera justificable.

## 23. Razones de limpieza

Los XML activos podían mostrar información ficticia antes de cargar datos o ante errores.
Se retiraron los strings login_left_phrase/login_right_phrase y el color black tras verificar referencias y Lint.
Se quitaron comentarios de plantilla/código XML comentado de backup sin alterar las exclusiones de sesión.
No se borraron entidades, migraciones, tests, Postman ni datos.

## 24. Refactorización

Políticas LWW centralizadas en Android y backend; mappers por recurso.
Motor separado del adaptador Room/Android para ejecutar tests sin dispositivo.
Observación de Room reutilizable y adapter de tarjetas compartido para Usuarios/Historial.
Se conservaron las interfaces públicas existentes de los repositories operativos.

## 25. Migraciones

No se modificó el schema de tablas: no se requiere migración.
No se añadió fallbackToDestructiveMigration.

## 26. Room

Versión de schema final: 1. Dependencia Room existente: 2.8.5.

## 27. Tests backend

37 tests, 0 failures, 0 errors, 0 skipped.
Incluyen los 32 existentes adaptados a los contratos nuevos y cinco pruebas adicionales:
LWW para los cinco recursos, prohibición a no-ADMIN, gestión ADMIN con BCrypt/no exposición,
validación de creación y revocación de rol con token previamente emitido.
Se ejecutaron contra H2 con el perfil test; no contra una base MariaDB real.

## 28. Tests Android/sync

18 tests, 0 failures, 0 errors, 0 skipped:
12 de motor/política HTTP/LWW/orden/concurrencia/estados, cinco de mappers y el test existente.
El orden real de SyncResources también se comprueba.
No se ejecutaron tests instrumentados ni WorkManager en un dispositivo.

## 29. Maven

Desde backend, con JAVA_HOME en JDK 21: `./mvnw.cmd clean package`.
Resultado final: BUILD SUCCESS. Tests run: 37, Failures: 0, Errors: 0, Skipped: 0.
Advertencias de Mockito/Byte Buddy sobre carga dinámica del agente.
Los errores SQL de las pruebas de restricciones son esperados y están comprobados por los tests.

## 30. Gradle

`./gradlew.bat assembleDebug testDebugUnitTest lintDebug`: BUILD SUCCESSFUL.
APK generado en app/build/outputs/apk/debug/app-debug.apk.
Lint final: 0 errors, 225 warnings, principalmente internacionalización, accesibilidad y versiones de dependencias.
Se corrigieron los dos errores iniciales de Lint en includeSubdomains de la configuración de red debug.
Permanece un aviso de API obsoleta en NewOrderFragment.

## 31–33. Git final

Las salidas literales de status, diff --stat y diff --check se incluyen en el anexo.
diff --stat describe archivos ya versionados; los archivos nuevos están enumerados aparte, sin staging.

## 34. Pruebas manuales pendientes

- Login online, reapertura offline y expiración de JWT.
- Crear/editar cliente, equipo, orden, diagnóstico y reparación con backend apagado.
- Cerrar el proceso y recuperar red para comprobar persistencia de WorkManager.
- Comprobar todos los datos descargados en Home/listas/detalles/Historial sin reabrir.
- Conflictos local más nuevo, remoto más nuevo e igualdad en dos dispositivos.
- Duplicados de serial/código y diagnóstico/reparación de distintos UUID para una misma orden.
- Corregir un registro ERROR y comprobar su vuelta a PENDING/SYNCED.
- Listar/crear/editar usuarios con ADMIN; comprobar 403 con TECNICO.
- Verificar tamaños de pantalla, teclado, rotación y navegación de los nuevos layouts.
- Integración completa Android → Spring Boot → MariaDB real.

## 35. Límites y funcionalidades incompletas

Los módulos pedidos tienen implementación, pero no se declara validación integral de todos los flujos en ejecución.
No se realizó QA manual ni se verificaron visualmente las pantallas en emulador.
ERROR muestra conteos y contexto; los conflictos de unicidad requieren corregir datos o seguimiento técnico.
No se creó un editor avanzado de conflictos. La descarga completa es deliberada para el volumen académico.
Las advertencias Lint restantes no se ocultaron ni se convirtieron en baseline.

## 36–41. Confirmaciones

Room sigue siendo la fuente de la UI operativa, incluyendo Home e Historial.
No se implementó DELETE sync, tombstones ni DELETED.
No se realizó deployment cloud.
No se hizo commit, push ni merge, ni cambio de rama.

## Inventario y salidas Git

El siguiente anexo contiene los archivos creados/modificados y las salidas finales.

### Archivos creados

```
text
ETAPA_3.md
app/src/main/java/com/tallertech/app/data/local/HistoryEntry.java
app/src/main/java/com/tallertech/app/data/local/LocalDataObserver.java
app/src/main/java/com/tallertech/app/data/remote/dto/auth/UserRequest.java
app/src/main/java/com/tallertech/app/data/repository/HistoryRepository.java
app/src/main/java/com/tallertech/app/data/repository/UserRepository.java
app/src/main/java/com/tallertech/app/data/sync/LastWriteWins.java
app/src/main/java/com/tallertech/app/data/sync/SyncCoordinator.java
app/src/main/java/com/tallertech/app/data/sync/SyncEngine.java
app/src/main/java/com/tallertech/app/data/sync/SyncHttp.java
app/src/main/java/com/tallertech/app/data/sync/SyncManager.java
app/src/main/java/com/tallertech/app/data/sync/SyncRepository.java
app/src/main/java/com/tallertech/app/data/sync/SyncResource.java
app/src/main/java/com/tallertech/app/data/sync/SyncResources.java
app/src/main/java/com/tallertech/app/data/sync/SyncStatus.java
app/src/main/java/com/tallertech/app/data/sync/SyncWorker.java
app/src/main/java/com/tallertech/app/data/sync/mapper/ClientMapper.java
app/src/main/java/com/tallertech/app/data/sync/mapper/DiagnosisMapper.java
app/src/main/java/com/tallertech/app/data/sync/mapper/EquipmentMapper.java
app/src/main/java/com/tallertech/app/data/sync/mapper/RepairMapper.java
app/src/main/java/com/tallertech/app/data/sync/mapper/ServiceOrderMapper.java
app/src/main/java/com/tallertech/app/ui/common/RecordAdapter.java
app/src/main/java/com/tallertech/app/ui/history/HistoryFragment.java
app/src/main/java/com/tallertech/app/ui/sync/SyncFragment.java
app/src/main/java/com/tallertech/app/ui/users/UsersFragment.java
app/src/main/res/drawable/bg_avatar.xml
app/src/main/res/layout/dialog_user.xml
app/src/main/res/layout/fragment_records.xml
app/src/main/res/layout/fragment_sync.xml
app/src/main/res/layout/item_record.xml
app/src/test/java/com/tallertech/app/data/sync/SyncEngineTest.java
app/src/test/java/com/tallertech/app/data/sync/SyncMapperTest.java
backend/src/main/java/com/tallertech/backend/service/LastWriteWins.java
backend/src/main/java/com/tallertech/backend/service/Roles.java
```

### Archivos con diferencias de contenido

```
text
app/build.gradle.kts
app/src/debug/res/xml/debug_network_security.xml
app/src/main/AndroidManifest.xml
app/src/main/java/com/tallertech/app/MainActivity.java
app/src/main/java/com/tallertech/app/data/local/dao/ClientDao.java
app/src/main/java/com/tallertech/app/data/local/dao/DiagnosisDao.java
app/src/main/java/com/tallertech/app/data/local/dao/EquipmentDao.java
app/src/main/java/com/tallertech/app/data/local/dao/RepairDao.java
app/src/main/java/com/tallertech/app/data/local/dao/ServiceOrderDao.java
app/src/main/java/com/tallertech/app/data/remote/api/TallerTechApi.java
app/src/main/java/com/tallertech/app/data/repository/AuthRepository.java
app/src/main/java/com/tallertech/app/data/repository/ClientRepository.java
app/src/main/java/com/tallertech/app/data/repository/DiagnosisRepository.java
app/src/main/java/com/tallertech/app/data/repository/EquipmentRepository.java
app/src/main/java/com/tallertech/app/data/repository/RepairRepository.java
app/src/main/java/com/tallertech/app/data/repository/RepositoryExecutor.java
app/src/main/java/com/tallertech/app/data/repository/ServiceOrderRepository.java
app/src/main/java/com/tallertech/app/data/repository/StatisticsRepository.java
app/src/main/java/com/tallertech/app/ui/clients/ClientDetailFragment.java
app/src/main/java/com/tallertech/app/ui/clients/ClientsFragment.java
app/src/main/java/com/tallertech/app/ui/equipment/EquipmentDetailFragment.java
app/src/main/java/com/tallertech/app/ui/equipment/EquipmentFragment.java
app/src/main/java/com/tallertech/app/ui/home/HomeFragment.java
app/src/main/java/com/tallertech/app/ui/orders/NewOrderFragment.java
app/src/main/java/com/tallertech/app/ui/orders/OrderDetailFragment.java
app/src/main/java/com/tallertech/app/ui/orders/OrdersFragment.java
app/src/main/java/com/tallertech/app/ui/profile/ProfileFragment.java
app/src/main/res/layout/fragment_client_detail.xml
app/src/main/res/layout/fragment_equipment_detail.xml
app/src/main/res/layout/fragment_profile.xml
app/src/main/res/layout/fragments_clients.xml
app/src/main/res/layout/item_client.xml
app/src/main/res/layout/item_equipment.xml
app/src/main/res/values/colors.xml
app/src/main/res/values/strings.xml
app/src/main/res/xml/backup_rules.xml
app/src/main/res/xml/data_extraction_rules.xml
backend/src/main/java/com/tallertech/backend/dto/UserRequest.java
backend/src/main/java/com/tallertech/backend/repository/ClientRepository.java
backend/src/main/java/com/tallertech/backend/repository/DiagnosisRepository.java
backend/src/main/java/com/tallertech/backend/repository/RepairRepository.java
backend/src/main/java/com/tallertech/backend/repository/ServiceOrderRepository.java
backend/src/main/java/com/tallertech/backend/security/JwtAuthenticationFilter.java
backend/src/main/java/com/tallertech/backend/security/SecurityConfig.java
backend/src/main/java/com/tallertech/backend/service/impl/ClientServiceImpl.java
backend/src/main/java/com/tallertech/backend/service/impl/DiagnosisServiceImpl.java
backend/src/main/java/com/tallertech/backend/service/impl/EquipmentServiceImpl.java
backend/src/main/java/com/tallertech/backend/service/impl/RepairServiceImpl.java
backend/src/main/java/com/tallertech/backend/service/impl/ServiceOrderServiceImpl.java
backend/src/main/java/com/tallertech/backend/service/impl/UserServiceImpl.java
backend/src/test/java/com/tallertech/backend/AuthSecurityTest.java
backend/src/test/java/com/tallertech/backend/BackendApiTest.java
```

### git status --short

```
text
 M app/build.gradle.kts
 M app/src/debug/res/xml/debug_network_security.xml
 M app/src/main/AndroidManifest.xml
 M app/src/main/java/com/tallertech/app/MainActivity.java
 M app/src/main/java/com/tallertech/app/data/local/dao/ClientDao.java
 M app/src/main/java/com/tallertech/app/data/local/dao/DiagnosisDao.java
 M app/src/main/java/com/tallertech/app/data/local/dao/EquipmentDao.java
 M app/src/main/java/com/tallertech/app/data/local/dao/RepairDao.java
 M app/src/main/java/com/tallertech/app/data/local/dao/ServiceOrderDao.java
 M app/src/main/java/com/tallertech/app/data/remote/api/TallerTechApi.java
 M app/src/main/java/com/tallertech/app/data/repository/AuthRepository.java
 M app/src/main/java/com/tallertech/app/data/repository/ClientRepository.java
 M app/src/main/java/com/tallertech/app/data/repository/DiagnosisRepository.java
 M app/src/main/java/com/tallertech/app/data/repository/EquipmentRepository.java
 M app/src/main/java/com/tallertech/app/data/repository/RepairRepository.java
 M app/src/main/java/com/tallertech/app/data/repository/RepositoryExecutor.java
 M app/src/main/java/com/tallertech/app/data/repository/ServiceOrderRepository.java
 M app/src/main/java/com/tallertech/app/data/repository/StatisticsRepository.java
 M app/src/main/java/com/tallertech/app/ui/clients/ClientDetailFragment.java
 M app/src/main/java/com/tallertech/app/ui/clients/ClientsFragment.java
 M app/src/main/java/com/tallertech/app/ui/equipment/EquipmentDetailFragment.java
 M app/src/main/java/com/tallertech/app/ui/equipment/EquipmentFragment.java
 M app/src/main/java/com/tallertech/app/ui/home/HomeFragment.java
 M app/src/main/java/com/tallertech/app/ui/orders/NewOrderFragment.java
 M app/src/main/java/com/tallertech/app/ui/orders/OrderDetailFragment.java
 M app/src/main/java/com/tallertech/app/ui/orders/OrdersFragment.java
 M app/src/main/java/com/tallertech/app/ui/profile/ProfileFragment.java
 M app/src/main/res/layout/fragment_client_detail.xml
 M app/src/main/res/layout/fragment_equipment_detail.xml
 M app/src/main/res/layout/fragment_profile.xml
 M app/src/main/res/layout/fragments_clients.xml
 M app/src/main/res/layout/item_client.xml
 M app/src/main/res/layout/item_equipment.xml
 M app/src/main/res/values/colors.xml
 M app/src/main/res/values/strings.xml
 M app/src/main/res/xml/backup_rules.xml
 M app/src/main/res/xml/data_extraction_rules.xml
 M backend/src/main/java/com/tallertech/backend/dto/UserRequest.java
 M backend/src/main/java/com/tallertech/backend/repository/ClientRepository.java
 M backend/src/main/java/com/tallertech/backend/repository/DiagnosisRepository.java
 M backend/src/main/java/com/tallertech/backend/repository/EquipmentRepository.java
 M backend/src/main/java/com/tallertech/backend/repository/RepairRepository.java
 M backend/src/main/java/com/tallertech/backend/repository/ServiceOrderRepository.java
 M backend/src/main/java/com/tallertech/backend/security/JwtAuthenticationFilter.java
 M backend/src/main/java/com/tallertech/backend/security/SecurityConfig.java
 M backend/src/main/java/com/tallertech/backend/service/impl/ClientServiceImpl.java
 M backend/src/main/java/com/tallertech/backend/service/impl/DiagnosisServiceImpl.java
 M backend/src/main/java/com/tallertech/backend/service/impl/EquipmentServiceImpl.java
 M backend/src/main/java/com/tallertech/backend/service/impl/RepairServiceImpl.java
 M backend/src/main/java/com/tallertech/backend/service/impl/ServiceOrderServiceImpl.java
 M backend/src/main/java/com/tallertech/backend/service/impl/UserServiceImpl.java
 M backend/src/test/java/com/tallertech/backend/AuthSecurityTest.java
 M backend/src/test/java/com/tallertech/backend/BackendApiTest.java
?? ETAPA_3.md
?? app/src/main/java/com/tallertech/app/data/local/HistoryEntry.java
?? app/src/main/java/com/tallertech/app/data/local/LocalDataObserver.java
?? app/src/main/java/com/tallertech/app/data/remote/dto/auth/UserRequest.java
?? app/src/main/java/com/tallertech/app/data/repository/HistoryRepository.java
?? app/src/main/java/com/tallertech/app/data/repository/UserRepository.java
?? app/src/main/java/com/tallertech/app/data/sync/
?? app/src/main/java/com/tallertech/app/ui/common/
?? app/src/main/java/com/tallertech/app/ui/history/
?? app/src/main/java/com/tallertech/app/ui/sync/
?? app/src/main/java/com/tallertech/app/ui/users/
?? app/src/main/res/drawable/bg_avatar.xml
?? app/src/main/res/layout/dialog_user.xml
?? app/src/main/res/layout/fragment_records.xml
?? app/src/main/res/layout/fragment_sync.xml
?? app/src/main/res/layout/item_record.xml
?? app/src/test/java/com/tallertech/app/data/
?? backend/src/main/java/com/tallertech/backend/service/LastWriteWins.java
?? backend/src/main/java/com/tallertech/backend/service/Roles.java
```

### git diff --stat

```
text
 app/build.gradle.kts                               |   1 +
 app/src/debug/res/xml/debug_network_security.xml   |   4 +-
 app/src/main/AndroidManifest.xml                   |   1 +
 .../main/java/com/tallertech/app/MainActivity.java |  12 ++
 .../tallertech/app/data/local/dao/ClientDao.java   |  13 ++-
 .../app/data/local/dao/DiagnosisDao.java           |  13 ++-
 .../app/data/local/dao/EquipmentDao.java           |  11 ++
 .../tallertech/app/data/local/dao/RepairDao.java   |  13 ++-
 .../app/data/local/dao/ServiceOrderDao.java        |  20 ++++
 .../app/data/remote/api/TallerTechApi.java         |   4 +
 .../app/data/repository/AuthRepository.java        |  34 +++++-
 .../app/data/repository/ClientRepository.java      |  26 +++--
 .../app/data/repository/DiagnosisRepository.java   |   5 +-
 .../app/data/repository/EquipmentRepository.java   |  23 +++-
 .../app/data/repository/RepairRepository.java      |   5 +-
 .../app/data/repository/RepositoryExecutor.java    |   4 +-
 .../data/repository/ServiceOrderRepository.java    |   6 +-
 .../app/data/repository/StatisticsRepository.java  |   4 +-
 .../app/ui/clients/ClientDetailFragment.java       |   4 +-
 .../tallertech/app/ui/clients/ClientsFragment.java |   8 +-
 .../app/ui/equipment/EquipmentDetailFragment.java  |   4 +-
 .../app/ui/equipment/EquipmentFragment.java        |   5 +-
 .../com/tallertech/app/ui/home/HomeFragment.java   |   6 +-
 .../tallertech/app/ui/orders/NewOrderFragment.java |   6 +-
 .../app/ui/orders/OrderDetailFragment.java         |   4 +-
 .../tallertech/app/ui/orders/OrdersFragment.java   |   4 +-
 .../tallertech/app/ui/profile/ProfileFragment.java |  19 ++-
 app/src/main/res/layout/fragment_client_detail.xml |   8 +-
 .../main/res/layout/fragment_equipment_detail.xml  |  12 +-
 app/src/main/res/layout/fragment_profile.xml       | 127 ++++++++++++++++-----
 app/src/main/res/layout/fragments_clients.xml      |   2 +-
 app/src/main/res/layout/item_client.xml            |   8 +-
 app/src/main/res/layout/item_equipment.xml         |   6 +-
 app/src/main/res/values/colors.xml                 |   1 -
 app/src/main/res/values/strings.xml                |   8 --
 app/src/main/res/xml/backup_rules.xml              |  13 +--
 app/src/main/res/xml/data_extraction_rules.xml     |  11 +-
 .../com/tallertech/backend/dto/UserRequest.java    |   8 +-
 .../backend/repository/ClientRepository.java       |   3 +
 .../backend/repository/DiagnosisRepository.java    |   3 +
 .../backend/repository/RepairRepository.java       |   3 +
 .../backend/repository/ServiceOrderRepository.java |   3 +
 .../backend/security/JwtAuthenticationFilter.java  |   2 +-
 .../backend/security/SecurityConfig.java           |   2 +-
 .../backend/service/impl/ClientServiceImpl.java    |   5 +-
 .../backend/service/impl/DiagnosisServiceImpl.java |   5 +-
 .../backend/service/impl/EquipmentServiceImpl.java |   3 +-
 .../backend/service/impl/RepairServiceImpl.java    |   5 +-
 .../service/impl/ServiceOrderServiceImpl.java      |   5 +-
 .../backend/service/impl/UserServiceImpl.java      |  14 ++-
 .../com/tallertech/backend/AuthSecurityTest.java   |  49 ++++++++
 .../com/tallertech/backend/BackendApiTest.java     |  30 ++++-
 52 files changed, 447 insertions(+), 148 deletions(-)
```

### git diff --check

Salida vacía; código de salida 0.

La entrada EquipmentRepository.java puede aparecer modificada por normalización de finales de línea, sin diferencia de contenido en diff.
