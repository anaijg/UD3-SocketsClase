# 📌 Chat multicliente en Java

Este chat multicliente en Java permite que varios clientes se comuniquen simultáneamente a través de un servidor central.  

Se ha diseñado utilizando hilos para manejar múltiples conexiones al mismo tiempo.

## 📌 1. Cómo funciona 
El chat sigue un **modelo cliente-servidor**, donde:
1. El **servidor** (`ChatServer.java`) espera conexiones de clientes y las acepta una por una.  
2. Cada **cliente** (`ChatClient.java`) se conecta al servidor y puede enviar/recibir mensajes en tiempo real.
3. Para gestionar los diferentes clientes que se conectan creamos un **manejador de clientes** (`ClientHandler.java`), que ejecuta un hilo por cada cliente, permitiendo que todos los clientes puedan chatear simultáneamente.
4. Cuando un cliente envía un mensaje, el servidor lo reenvía a todos los demás clientes conectados (**broadcast)**.

## 📌2. Elementos nuevos que se introducen aquí

Esta versión del chat introduce varias técnicas avanzadas en Java:  

| Concepto                 | Explicación                                                                            |
|--------------------------|----------------------------------------------------------------------------------------|
| **Hilos (`Thread`)**     | Se usa `ClientHandler` como un hilo para gestionar múltiples clientes simultáneamente. |
| **`ClientHandler`**      | Hilo que maneja un cliente, permitiendo múltiples conexiones simultáneas.              |
| **synchronized**         | Evita problemas de concurrencia cuando múltiples hilos modifican la lista de clientes. |
| **Método `broadcast()`** | Envía mensajes a todos los clientes conectados, menos al remitente.                    |
| **Hilo en el cliente**   | Permite recibir mensajes en segundo plano mientras el usuario escribe.                 |

## 📌 3. La clase `ClientHandler`
- En este chat, cada cliente conectado necesita un hilo independiente para poder recibir y enviar mensajes sin bloquear el servidor. 
- Para ello, creamos la clase `ClientHandler`, que extiende `Thread` y gestiona la comunicación con un cliente en particular.

### 🔍 Ejemplo de ClientHandler
````java

// Clase que maneja la comunicación con un cliente individual
public class ClientHandler extends Thread {
    private final Socket socket; // Socket del cliente
    private final Set<ClientHandler> clientes; // Referencia al conjunto de clientes conectados
    private DataInputStream input; // Flujo de entrada del cliente
    private DataOutputStream output; // Flujo de salida del cliente

    public ClientHandler(Socket socket, Set<ClientHandler> clientes) {
        this.socket = socket;
        this.clientes = clientes;
    }

    @Override
    public void run() {
        try {
            input = new DataInputStream(socket.getInputStream()); // Inicializa el flujo de entrada
            output = new DataOutputStream(socket.getOutputStream()); // Inicializa el flujo de salida

            String nombre = input.readUTF(); // Primer mensaje del cliente es su nombre
            System.out.println(nombre + " se ha unido al chat.");
            ChatServer.broadcast(nombre + " se ha unido al chat.", this, clientes);

            String mensaje;
            while (true) {
                mensaje = input.readUTF(); // Lee el mensaje enviado por el cliente

                if (mensaje.equalsIgnoreCase("salir")) { // Si el cliente escribe "salir", se desconecta
                    System.out.println(nombre + " ha salido del chat.");
                    ChatServer.broadcast(nombre + " ha salido del chat.", this, clientes);
                    cerrarConexion();
                    break;
                }

                System.out.println(nombre + ": " + mensaje);
                ChatServer.broadcast(nombre + ": " + mensaje, this, clientes); // Envía el mensaje a todos los clientes
            }
        } catch (IOException e) {
            System.out.println("Error en la comunicación con un cliente.");
        }
    }

    // Método para enviar un mensaje a un cliente
    public void enviarMensaje(String mensaje) {
        try {
            output.writeUTF(mensaje);
        } catch (IOException e) {
            System.out.println("Error enviando mensaje a un cliente.");
        }
    }

    // Método para cerrar la conexión del cliente
    private void cerrarConexion() {
        synchronized (clientes) { // Asegura que la eliminación del cliente sea segura
            clientes.remove(this);
        }
        try {
            socket.close(); // Cierra el socket del cliente
        } catch (IOException e) {
            System.out.println("Error al cerrar la conexión con el cliente.");
        }
    }
}
````
#### 1️⃣ `ClientHandler` extiende `Thread`
````java
public class ClientHandler extends Thread {
````
- **¿Por qué `extends Thread`?**
- Cada cliente necesita ejecutarse en un hilo separado para que la comunicación con múltiples clientes sea simultánea.  
- `Thread` permite que cada conexión de cliente se maneje de forma independiente, evitando que un cliente bloquee a los demás.

✅ Ventaja: Permite que varios clientes puedan conectarse y enviar mensajes sin interferencias.
❌ Alternativa: Podría usarse `Runnable`, pero con `Thread` es más fácil de manejar en este contexto.

#### 2️⃣ Atributos de la Clase
````java
private final Socket socket; // Socket del cliente
private final Set<ClientHandler> clientes; // Referencia al conjunto de clientes conectados
private DataInputStream input; // Flujo de entrada del cliente
private DataOutputStream output; // Flujo de salida del cliente
````
- `socket`: Representa la conexión del cliente con el servidor.
- `clientes`: Mantiene una referencia a todos los clientes conectados, lo que permite enviar mensajes a otros clientes.
- `input` y `output`: Se usan para leer y escribir datos en la conexión con el cliente.

✅ Permite que cada cliente tenga su propio canal de comunicación con el servidor.

#### 3️⃣ Constructor de `ClientHandler`
````java
public ClientHandler(Socket socket, Set<ClientHandler> clientes) {
    this.socket = socket;
    this.clientes = clientes;
}
````
- Recibe el `Socket` del cliente para gestionar su conexión.
- Recibe el `Set<ClientHandler>` para poder acceder a la lista de clientes conectados y retransmitir mensajes.

✅ Esto permite que el servidor mantenga el control de todos los clientes y pueda enviarles mensajes.

#### 4️⃣ Método `run()`: el núcleo del Cliente

El método `run()` define lo que hará cada cliente cuando se conecte al servidor.

````java
@Override
public void run() {
    try {
        input = new DataInputStream(socket.getInputStream()); // Inicializa el flujo de entrada
        output = new DataOutputStream(socket.getOutputStream()); // Inicializa el flujo de salida
````
- `input = new DataInputStream(socket.getInputStream());`  
    - Permite leer mensajes enviados por el cliente.  
- `output = new DataOutputStream(socket.getOutputStream());`  
    - Permite enviar mensajes al cliente.

✅ Cada cliente tiene su propio canal de entrada y salida de datos.

#### 5️⃣ El cliente envía su Nombre
````java
String nombre = input.readUTF(); // Primer mensaje del cliente es su nombre
System.out.println(nombre + " se ha unido al chat.");
ChatServer.broadcast(nombre + " se ha unido al chat.", this, clientes);
````

- `input.readUTF();`: El cliente envía su nombre al conectarse.  
- Se muestra en la consola del servidor (`System.out.println(nombre + " se ha unido al chat.");`).
- Se retransmite el mensaje a todos los clientes usando `broadcast()`.

✅ Esto notifica a todos que un nuevo usuario se ha unido al chat.

#### 6️⃣ Bucle de Comunicación con el Cliente

El siguiente fragmento mantiene la conexión activa **hasta que el cliente escriba "salir"**.
````java
String mensaje;
while (true) {
mensaje = input.readUTF(); // Lee el mensaje enviado por el cliente

    if (mensaje.equalsIgnoreCase("salir")) { // Si el cliente escribe "salir", se desconecta
        System.out.println(nombre + " ha salido del chat.");
        ChatServer.broadcast(nombre + " ha salido del chat.", this, clientes);
        cerrarConexion();
        break;
    }

    System.out.println(nombre + ": " + mensaje);
    ChatServer.broadcast(nombre + ": " + mensaje, this, clientes); // Envía el mensaje a todos los clientes
}
````
##### 🛠 ¿Cómo Funciona?
1. Lee un mensaje del cliente (`input.readUTF()`).
2. Si el mensaje es "salir", el cliente se desconecta:
    - Muestra un mensaje en el servidor (`System.out.println()`).  
    - Informa a los demás clientes con `broadcast()`.
    - Llama a `cerrarConexion()`.  
    - Finaliza el bucle con `break`.
3. Si no es "salir", reenvía el mensaje a todos los demás clientes con `ChatServer.broadcast()`.

✅ El chat se mantiene activo hasta que el usuario decide salir.
#### 7️⃣ Método `enviarMensaje()`

Este método envía un mensaje a un cliente en particular.
````java
public void enviarMensaje(String mensaje) {
    try {
        output.writeUTF(mensaje);
    } catch (IOException e) {
        System.out.println("Error enviando mensaje a un cliente.");
    }
}
````
- `output.writeUTF(mensaje);`  
    - Permite enviar un mensaje de texto al cliente.
      Se usa en `broadcast()` para reenviar mensajes entre clientes.
✅ Permite que los clientes reciban mensajes de los demás.

#### 8️⃣ Método `cerrarConexion()`

Cuando un cliente se desconecta, este método cierra la conexión y lo elimina de la lista de clientes activos.
````java
private void cerrarConexion() {
    synchronized (clientes) { // Asegura que la eliminación del cliente sea segura
        clientes.remove(this);
    }
    try {
        socket.close(); // Cierra el socket del cliente
    } catch (IOException e) {
        System.out.println("Error al cerrar la conexión con el cliente.");
    }
}
````
##### 🛠 ¿Qué hace este método?
1. Elimina el cliente de la lista global (`clientes`)
    - `synchronized (clientes) { clientes.remove(this); }`
    - Evita errores si varios clientes se desconectan a la vez.  
2. Cierra la conexión con el cliente (`socket.close();`)
    - Libera recursos de red.

✅ El servidor ya no intentará enviarle mensajes y su socket se libera.

#### 9. Conclusión

`ClientHandler` es la pieza clave que permite la comunicación entre clientes en un chat multicliente.  

| Elemento                          | Explicación |
|-----------------------------------|-------------------------------------------|
| **Extiende `Thread`**             | Permite ejecutar cada cliente en un hilo separado. |
| **Maneja `socket`, `input` y `output`** | Cada cliente tiene su propio canal de comunicación. |
| **Guarda la lista de clientes (`clientes`)** | Permite interactuar con otros clientes conectados. |
| **Método `run()`**                | Controla el flujo de mensajes entre cliente y servidor. |
| **Método `broadcast()` (en `ChatServer`)** | Reenvía mensajes a todos los clientes. |
| **Método `enviarMensaje()`**      | Envía un mensaje a un cliente en particular. |
| **Método `cerrarConexion()`**     | Elimina al cliente de la lista y cierra su socket. |

## 📌 La clase `ChatServer`
La clase `ChatServer` es el núcleo del servidor del chat multicliente. Su objetivo principal es gestionar las conexiones de los clientes, recibirlos y asegurarse de que puedan comunicarse entre sí mediante el método `broadcast()`.

### 1️⃣ Definición de la clase ChatServer
````java
public class ChatServer {
````
- `ChatServer` es la clase principal del servidor.
- Se encarga de:
    - Aceptar conexiones de clientes.
    - Crear una instancia de `ClientHandler` para cada cliente.
    - Mantener la lista de clientes conectados.
    - Reenviar mensajes entre clientes con `broadcast()`.

✅ Es el "punto central" de toda la aplicación de chat.

### 2️⃣ Declaración de constantes y variables
````java
public static final int PORT = 12345; // Puerto donde el servidor escuchará conexiones
private static final Set<ClientHandler> clientes = new HashSet<>(); // Lista de clientes conectados
````
**📌 ¿Qué significan estas variables?**
- `PORT` → Es el puerto en el que el servidor esperará conexiones de los clientes (12345 en este caso).
- clientes → Es una lista (conjunto) de clientes activos, implementada como un `Set<ClientHandler>`:
    - Usamos `HashSet<>` para evitar clientes duplicados.  
    - Es `static` porque debe compartirse entre todas las instancias de `ClientHandler`.

✅ Esto permite que el servidor recuerde a todos los clientes conectados.
### 3️⃣ Método main(): punto de entrada del servidor
````java
public static void main(String[] args) {
````
- Es el punto de inicio de la ejecución del servidor.
- Inicia el `ServerSocket` y espera conexiones de clientes.

✅ Sin este método, el servidor no funcionaría.
### 4️⃣ Creación del ServerSocket y Aceptación de Clientes
````java
try (ServerSocket servidor = new ServerSocket(PORT)) {
System.out.println("Servidor de chat en línea en el puerto " + PORT);

    new ServerSocket(PORT);
````
- Crea un socket del servidor en el puerto 12345, permitiendo que los clientes se conecten.
- El try-with-resources asegura que el `ServerSocket` se cierre correctamente cuando el programa termine.

✅ El servidor ahora está listo para aceptar clientes.
### 5️⃣ Bucle Infinito para Aceptar Conexiones
````java
while (true) { // Bucle infinito para aceptar conexiones de clientes
Socket socket = servidor.accept(); // Espera a que un cliente se conecte
System.out.println("Nuevo cliente conectado.");
````
- `while (true)`  
    - Permite que el servidor nunca se detenga y siga aceptando clientes indefinidamente.
- `servidor.accept();`  
  - Bloquea la ejecución hasta que un cliente se conecte.
      Devuelve un `Socket` representando la conexión con ese cliente.

✅ Cada vez que un cliente se conecta, se crea un nuevo Socket para gestionarlo.
### 6️⃣ Creación de un `ClientHandler` para cada cliente
````java
ClientHandler nuevoCliente = new ClientHandler(socket, clientes);
````
- Se crea una nueva instancia de `ClientHandler` para gestionar el cliente recién conectado. 
- `socket` → Representa la conexión con este cliente.
- `clientes` → Se pasa la referencia al `Set<ClientHandler>` para que `ClientHandler` pueda acceder a los demás clientes conectados.

✅ Cada cliente tiene su propio `ClientHandler`, ejecutándose en un hilo separado.

### 7️⃣ Protección de la lista de clientes con `synchronized`
````java
synchronized (clientes) { // Bloquea la lista de clientes para evitar problemas de concurrencia
    clientes.add(nuevoCliente);
}
````
**¿Por qué synchronized?**  
Como varios clientes pueden conectarse simultáneamente, debemos evitar que dos hilos modifiquen la lista `clientes` al mismo tiempo.  
`synchronized (clientes)` asegura que solo un cliente a la vez puede ser agregado o eliminado.

✅ Evita errores cuando múltiples clientes entran o salen al mismo tiempo.
### 8️⃣ Inicio del `ClientHandler` en un hilo separado
````java
nuevoCliente.start(); // Inicia el hilo para gestionar al cliente
````
- `start()` → Lanza el hilo del cliente (`ClientHandler`).
    Cada cliente ahora se maneja en su propio hilo y puede enviar/recibir mensajes sin bloquear a los demás.

✅ Esto permite que muchos clientes se comuniquen simultáneamente. 
### 9️⃣ Manejo de Excepciones (catch)
````java
} catch (IOException e) {
    System.out.println("Error en el servidor: " + e.getMessage());
    e.printStackTrace();
}
````
- Si ocurre un error al iniciar el servidor o aceptar clientes, se muestra un mensaje de error.
- `e.printStackTrace();` ayuda a depurar mostrando la causa exacta del error.

✅ El servidor sigue funcionando aunque ocurra un error con un cliente.
### 🔟 Método `broadcast():` enviar mensajes a todos los clientes
````java
public static void broadcast(String mensaje, ClientHandler remitente, Set<ClientHandler> clientes) {
synchronized (clientes) { // Bloqueo para evitar modificaciones simultáneas
        for (ClientHandler cliente : clientes) {
            if (cliente != remitente) { // No enviamos el mensaje al remitente
                cliente.enviarMensaje(mensaje);
            }
        }
    }
}
````
**📌 ¿Cómo funciona broadcast()?**
- Recibe un mensaje y el remitente (`ClientHandler`).  
- Bloquea la lista de clientes con `synchronized` para evitar modificaciones simultáneas.  
- Recorre todos los clientes conectados.  
- Envía el mensaje a todos los clientes, excepto al remitente.

✅ Permite que todos los clientes reciban los mensajes enviados por los demás.
### 📌 Conclusión

ChatServer es el núcleo del chat multicliente. Se encarga de aceptar conexiones y gestionar la comunicación entre clientes mediante `broadcast()`.   

| Elemento                          | Explicación |
|-----------------------------------|-------------------------------------------|
| **Crea `ServerSocket`**           | Permite que los clientes se conecten al servidor. |
| **Bucle `while (true)`**          | Mantiene el servidor siempre activo, esperando clientes. |
| **Acepta conexiones (`accept()`)** | Bloquea hasta que un cliente se conecta. |
| **Crea `ClientHandler`**          | Maneja la comunicación con cada cliente en un hilo separado. |
| **Usa `synchronized` en `clientes`** | Evita errores de concurrencia al modificar la lista de clientes. |
| **Método `broadcast()`**          | Envía mensajes a todos los clientes conectados, excepto al remitente. |

## 📌 La clase `ChatClient`
La clase `ChatClient` es el programa que ejecuta cada usuario para conectarse al servidor de chat multicliente.  

Su propósito principal es:
- Conectarse al servidor (ChatServer).
- Enviar mensajes al servidor.
- Recibir mensajes de otros clientes a través del servidor.
- Permitir que el usuario salga del chat escribiendo "salir".

A continuación, desglosamos los puntos más relevantes de su implementación:
### 1️⃣ Método main(): punto de entrada del cliente
````java
public static void main(String[] args) {
````
- Es el punto de inicio de la ejecución del cliente.
- Se encarga de gestionar la conexión con el servidor.
- Permite la interacción del usuario con la terminal.

✅ Sin este método, el cliente no podría iniciar su conexión al chat.
### 2️⃣ Creación del Socket y Flujos de Entrada/Salida
````java
try (Socket socket = new Socket(SERVER, PORT); // Se conecta al servidor
    DataInputStream input = new DataInputStream(socket.getInputStream()); // Flujo de entrada
    DataOutputStream output = new DataOutputStream(socket.getOutputStream()); // Flujo de salida
    Scanner scanner = new Scanner(System.in)) { // Escáner para leer la entrada del usuario
````
**📌 ¿Qué hace este bloque?**
- `new Socket(SERVER, PORT);`  
  - Conecta el cliente al servidor en la dirección SERVER y el puerto PORT (127.0.0.1:12345 por defecto).
        Si el servidor no está activo, lanzará una excepción IOException.
- `DataInputStream input = new DataInputStream(socket.getInputStream());`
    - Permite recibir datos desde el servidor.

- `DataOutputStream output = new DataOutputStream(socket.getOutputStream());`
    - Permite enviar datos al servidor.

- `Scanner scanner = new Scanner(System.in);`
    - Permite que el usuario escriba mensajes en la terminal.

✅ Estos flujos permiten la comunicación entre el cliente y el servidor.
### 3️⃣ Solicitud del Nombre del Usuario
````java
System.out.print("Introduce tu nombre: ");
String nombre = scanner.nextLine();
output.writeUTF(nombre); // Envía el nombre al servidor
````
- El cliente pide el nombre del usuario. 
- El nombre se envía al servidor (`output.writeUTF(nombre);`).
- El servidor lo utilizará para identificar al usuario en el chat.

✅ Así, los demás clientes sabrán quién envía cada mensaje.
### 4️⃣ Hilo para recibir mensajes del servidor
````java
Thread receptorMensajes = new Thread(() -> {
    try {
        while (true) {
            String mensaje = input.readUTF(); // Recibe mensajes del servidor
            System.out.println("\n" + mensaje); // Muestra el mensaje en la consola
            System.out.print("> "); // Mantiene la línea de entrada limpia
        }
    } catch (IOException e) {
    System.out.println("Desconectado del servidor.");
    }
});
````
### 📌 ¿Por qué necesitamos un hilo separado?
- El cliente debe poder escribir mensajes y recibir mensajes al mismo tiempo.
- Si no hubiera un hilo, el cliente tendría que esperar un mensaje antes de poder escribir otro.

##### 🛠 ¿Cómo funciona este hilo?
- Ejecuta un bucle infinito `(while (true))` para escuchar mensajes del servidor.
- Usa `input.readUTF();` para leer mensajes enviados por el servidor.
- Muestra el mensaje en pantalla (System.out.println(mensaje);).
- Si el servidor se desconecta, atrapa la excepción y muestra un mensaje de error.

✅ El cliente puede recibir mensajes en segundo plano mientras sigue escribiendo.
### 5️⃣ Inicio del hilo receptor de mensajes
- `receptorMensajes.start(); // Inicia el hilo que escucha los mensajes del servidor`
    - Ejecuta el hilo que escucha los mensajes del servidor.
    - Este hilo se ejecuta en paralelo con el código principal.

✅ Permite que el usuario siga escribiendo mientras recibe mensajes de otros clientes.
### 6️⃣ Bucle para enviar mensajes al servidor
````java
while (true) {
System.out.print("> "); // Indicación para escribir un mensaje
String mensajeAlServidor = scanner.nextLine(); // Captura el mensaje del usuario
````
- Muestra el símbolo > para que el usuario escriba.
- Captura el mensaje ingresado (scanner.nextLine();).

✅ El usuario puede enviar mensajes al chat de forma continua.
### 7️⃣ Condición para salir del chat
````java
if (mensajeAlServidor.equalsIgnoreCase("salir")) { // Si el usuario escribe "salir", se desconecta
    output.writeUTF("salir");
    System.out.println("Has salido del chat.");
    break;
}
````
**📌 ¿Cómo el cliente se desconecta?**
- Si el usuario escribe "salir", se ejecuta este bloque.
- Se envía "salir" al servidor (`output.writeUTF("salir");`).
- El mensaje Has salido del chat. se muestra en la terminal.
- `break;` rompe el bucle, finalizando la ejecución del cliente.

✅ Permite que el usuario salga del chat de forma natural.
### 8️⃣ Envío del Mensaje al Servidor
````java
output.writeUTF(mensajeAlServidor); // Envía el mensaje al servidor
````
- Todos los mensajes ingresados se envían al servidor.
- El servidor los reenvía a los demás clientes usando `broadcast()`.

✅ Cada mensaje ingresado se transmite a todos los clientes conectados.  
### 9️⃣ Manejo de Errores (catch)
````java
} catch (IOException e) {
    System.out.println("Error al conectar con el servidor.");
}
````
- Si ocurre un error (por ejemplo, si el servidor no está disponible), se muestra un mensaje de error.
- Esto evita que el programa falle abruptamente.

✅ El cliente maneja errores de conexión de forma segura.
### 📌 Conclusión

`ChatClient` permite que un usuario se conecte al servidor de chat multicliente y se comunique con otros clientes.   

| Elemento                                | Explicación |
|-----------------------------------------|-------------------------------------------|
| **Crea un `Socket`**                    | Conecta al cliente con el servidor en el puerto `12345`. |
| **Usa `DataInputStream` y `DataOutputStream`** | Permite enviar y recibir mensajes. |
| **Solicita el nombre del usuario**      | Permite identificar al usuario en el chat. |
| **Crea un hilo separado (`Thread`)**    | Permite recibir mensajes en tiempo real mientras el usuario escribe. |
| **Bucle para enviar mensajes**          | Captura y envía mensajes al servidor continuamente. |
| **Permite salir con `"salir"`**         | Cierra la conexión cuando el usuario lo solicita. |

