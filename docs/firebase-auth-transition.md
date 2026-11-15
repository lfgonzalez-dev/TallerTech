# Etapa 2: Firebase Authentication y compatibilidad temporal

## Flujo

`LoginActivity → LoginViewModel → AuthRepository → FirebaseAuthRepository`.
Solo `FirebaseAuthRepository` accede al SDK Firebase Auth. Usa Email/Password,
expone usuario (UID, email, nombre), estado observable y logout. Firebase SDK
administra la sesión; no guardamos contraseñas ni tokens Firebase.

Después de `signInWithEmailAndPassword`, `AuthRepository` intenta obtener el JWT
en `LegacyAuthBridge`, usando las mismas credenciales únicamente en memoria.
El backend sigue recibiendo `/api/auth/login`; sus endpoints no cambian.
Esta petición temporal puede demorar hasta el timeout existente de Retrofit
(25 segundos), pero su fallo no rechaza una autenticación Firebase correcta.

Al iniciar, se usa la sesión persistida por Firebase y `reload()` para detectar
cuentas deshabilitadas/eliminadas cuando hay conexión. Un fallo transitorio
conserva el acceso local. Luego se intenta `/api/auth/me` si hay JWT vinculado
al mismo UID. Sin sesión Firebase, un JWT antiguo no permite entrar. Si Android
restaura directamente MainActivity después de matar el proceso, se vuelve al
flujo de validación de LoginActivity antes de mostrar los módulos.

## Por qué sigue existiendo JWT

Spring Boot todavía exige Bearer JWT para usuarios y para sincronizar clientes,
equipos, órdenes, diagnósticos y reparaciones. `SyncCoordinator` y
`AuthInterceptor` comprueban que el JWT pertenezca al UID Firebase activo.
El JWT sigue cifrado con AES-GCM y Android Keystore; su UID asociado se guarda
en las mismas preferencias `secure_session`, ya excluidas de backup/transferencia.
Un 401 limpia el JWT afectado; nunca cierra la sesión Firebase. Una respuesta
401 antigua no borra un token nuevo distinto. Los trabajos de sincronización
sin JWT fallan conservando los registros locales pendientes y muestran un aviso.

La administración de usuarios y sus roles siguen siendo del backend. Una cuenta
solo Firebase no recibe rol administrativo. El perfil Firebase es una proyección
en memoria para la UI, no un registro nuevo en Room. `UserEntity`, sus IDs del
backend, DAOs, esquema, versión de base y datos locales permanecen sin cambios.
No hace falta añadir `firebaseUid` a Room todavía: no se persiste ninguna relación
de negocio con Firebase en esta etapa.

El logout cierra Firebase, invalida el estado en memoria, cancela el trabajo de
sincronización y limpia el JWT en el ejecutor de autenticación. Los resultados
tardíos se descartan usando la generación de sesión y el UID. Una petición que
ya llegó al servidor no puede deshacerse al cerrar sesión.

## Preparación manual y límites

- Habilitar Email/Password en Firebase Console y crear previamente las cuentas.
- Para mantener operaciones remotas, la misma persona debe tener una cuenta
  válida en ambos sistemas, con el mismo correo y contraseña. No se copian ni se
  crean cuentas automáticamente. Cambiar la contraseña solo en uno de los dos
  sistemas interrumpe la compatibilidad JWT, pero no el acceso Firebase/local.
- Si falla la compatibilidad, corregir las credenciales y cerrar/iniciar sesión
  para reintentar. No se guarda la contraseña para renovar un JWT en segundo plano.
- El JWT que existía antes de esta etapa no tiene UID asociado y se descarta;
  es necesario iniciar sesión con Firebase una primera vez.
- No se verificaron ni cambiaron proveedores/cuentas en Console desde el código.
- No se necesita Firestore, reglas, Admin SDK, service accounts ni cambios del backend.
- Room conserva una base de taller compartida entre cuentas, como antes. Esta etapa
  no implementa aislamiento por usuario ni borra datos locales al hacer logout.
- La revocación de cuentas Firebase requiere conexión para ser detectada; sin red
  se conserva la sesión local administrada por el SDK.

Al migrar datos a Firestore se deberá retirar `LegacyAuthBridge`, el almacenamiento
JWT y sus comprobaciones en interceptor/sincronización, y sustituir la fuente de
roles del backend. No enviar tokens Firebase a Spring Boot: el backend actual no
los valida.

## Validación manual cuando haya un dispositivo y una cuenta de prueba

1. Login con cuenta Firebase válida y con credenciales incorrectas/deshabilitadas.
2. Login válido en Firebase y backend; confirmar sincronización y roles actuales.
3. Login solo Firebase o backend caído: acceso local y aviso de compatibilidad.
4. Relanzar con sesión activa, también sin red y después de matar el proceso.
5. Logout y regreso al login; relanzar y comprobar que no queda sesión.
6. Cambiar de cuenta y verificar que no se reutiliza el JWT anterior.

Referencias: [Email/Password](https://firebase.google.com/docs/auth/android/password-auth)
y [sesión de usuario](https://firebase.google.com/docs/auth/android/manage-users).
