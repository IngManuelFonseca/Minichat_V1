# Minichat

Mini chat de escritorio hecho en **Java (Swing)** que conecta dos ventanas mediante **sockets** y guarda los mensajes en una base de datos **SQLite** durante un tiempo limitado.

## Características

- Chat entre dos usuarios (Usuario 1 y Usuario 2) comunicados por sockets TCP.
- Los mensajes se guardan en una base de datos SQLite con remitente, texto y fecha/hora.
- Al abrir el chat se carga el historial guardado.
- Los mensajes con más de 24 horas se borran automáticamente (al iniciar y cada 10 minutos).
- Interfaz con burbujas de mensaje, hora en cada mensaje y scroll automático.
- Aviso en pantalla si el otro usuario no está conectado.

## Tecnologías

- Java
- Swing (interfaz gráfica)
- Sockets TCP (`ServerSocket` / `Socket`)
- SQLite con JDBC (`sqlite-jdbc`)
- NetBeans

## Estructura del proyecto

| Archivo | Función |
|---|---|
| `Main.java` | Abre las dos ventanas del chat a la vez |
| `VentanaChat.java` | Interfaz del chat (burbujas, envío y recepción) |
| `formulario01.java` / `formulario02.java` | Usuario 1 (puerto 5000) y Usuario 2 (puerto 6000) |
| `Servidor.java` | Escucha mensajes entrantes en un puerto |
| `Cliente.java` | Envía mensajes al otro usuario |
| `Mensaje.java` | Modelo de un mensaje (remitente, texto, fecha) |
| `BaseDatos.java` | Conexión a SQLite, guardado, historial y borrado de mensajes viejos |

## Requisitos

- JDK instalado
- NetBeans (o cualquier IDE de Java)
- Librerías en la carpeta `lib/`:
  - [`sqlite-jdbc`](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/)
  - [`slf4j-api`](https://repo1.maven.org/maven2/org/slf4j/slf4j-api/)

## Cómo ejecutarlo

1. Clona el repositorio y ábrelo en NetBeans.
2. Agrega los `.jar` de `lib/` al proyecto: clic derecho en el proyecto > Properties > Libraries > Add JAR/Folder.
3. Ejecuta `Main.java` (clic derecho > Run File). Se abren las dos ventanas del chat.

La base de datos `minichat.db` se crea automáticamente en la carpeta del proyecto.

## Configuración

El tiempo que se guardan los mensajes se cambia en `BaseDatos.java`:

```java
public static final int HORAS_RETENCION = 24;
```

## Autor

Manuel Fonseca - Proyecto académico
