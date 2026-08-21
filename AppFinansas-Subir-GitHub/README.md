# App Finansas

**Estudiante:** Shirley *(completa tu nombre completo aquí)*

## Descripción

Aplicación nativa Android para el registro de finanzas personales en **tiempo real**. Permite registrar ingresos y gastos, visualizarlos en un RecyclerView sincronizado con **Firebase Firestore**, editar y eliminar registros desde cualquier dispositivo conectado.

## Funcionalidades

- **Sincronización en tiempo real** con Firestore (`SnapshotListener`).
- **CRUD completo en NoSQL**: crear, leer (POJO), actualizar y eliminar por Document ID.
- **RecyclerView** con adaptador personalizado y ViewHolder.
- **CardViews** con sombras, bordes redondeados e íconos vectoriales.
- **Formularios Material Design** con `TextInputLayout`.
- **Validaciones en tiempo real** (`TextWatcher`) y prevención de doble envío.
- **Liberación de memoria** Firebase en `onDestroy()`.

## Estructura del proyecto

```
app/src/main/java/com/example/appfinansas/
├── MainActivity.java                 # Lista en tiempo real (SnapshotListener)
├── TransaccionFormActivity.java      # Formulario CRUD + validaciones
├── adapter/
│   └── TransaccionAdapter.java       # Adaptador con ViewHolder
├── firebase/
│   └── FirestoreRepository.java      # Operaciones Firestore
└── model/
    └── Transaccion.java              # POJO mapeado desde Firestore
```

## Firebase Firestore

**Colección:** `transacciones`

| Campo          | Tipo    | Descripción                    |
|----------------|---------|--------------------------------|
| concepto       | String  | Descripción del movimiento     |
| monto          | double  | Valor de la transacción        |
| tipo           | int     | 1 = Ingreso, 2 = Gasto         |
| fechaCreacion  | Timestamp | Ordenamiento y sincronización |

El **Document ID** de Firestore se usa para editar y eliminar registros.

## Configuración de Firebase

1. Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
2. Agrega una app Android con package `com.example.appfinansas`.
3. Descarga `google-services.json` y reemplaza el archivo en `app/google-services.json`.
4. En Firestore, crea la base de datos en **modo prueba** (o configura reglas de seguridad).
5. Sincroniza Gradle y ejecuta la app.

### Reglas de Firestore (desarrollo)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /transacciones/{document=**} {
      allow read, write: if true;
    }
  }
}
```

> Para producción, restringe el acceso con autenticación.

## Capturas de pantalla

> Agrega tus capturas en `docs/screenshots/`.

### Lista de transacciones
![Lista de transacciones](docs/screenshots/lista_transacciones.png)

### Formulario de registro
![Formulario de registro](docs/screenshots/formulario_transaccion.png)

## Requisitos

- Android Studio (Ladybug o superior)
- JDK 11+
- minSdk 24
- Cuenta Firebase con Firestore habilitado

## Cómo ejecutar

1. Clona el repositorio desde GitHub.
2. Configura Firebase (`google-services.json`).
3. Abre el proyecto en Android Studio.
4. Sincroniza Gradle.
5. Ejecuta en emulador o dispositivo con internet.

## Tecnologías

- Java
- Firebase Firestore
- RecyclerView + ViewHolder
- Material Design Components (TextInputLayout, CardView)

## Entrega (GitHub)

1. Inicializa Git: `git init`
2. Commit: `git add . && git commit -m "Migración a Firebase Firestore"`
3. Crea repositorio en GitHub y sube: `git remote add origin <url> && git push -u origin main`

Incluye `.gitignore` (excluye `/build`, `/.idea`, etc.) y este README con tu nombre completo y capturas.
