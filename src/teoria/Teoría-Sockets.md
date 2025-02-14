# Sockets en Java

Los sockets permiten que las aplicaciones se comuniquen a través de una red, ya sea en la misma máquina o en diferentes ubicaciones. Esto posibilita la creación de aplicaciones distribuidas como servidores web, bases de datos, salas de chat y más. En este tema, aprenderemos sobre las dos clases principales para la comunicación entre programas que usan sockets: `Socket` y `ServerSocket`. También escribiremos nuestro propio servidor multihilo.

## ¿Qué es un socket?

Cada computadora en Internet tiene una dirección única que la identifica y permite la interacción con otras computadoras.

Si necesitas escribir una aplicación en red como un mensajero instantáneo o un juego en línea, debes organizar la interacción entre múltiples usuarios. Esto se puede lograr mediante sockets.

**Un socket es una interfaz para enviar y recibir datos entre procesos en forma bidireccional**. Está determinado por la combinación de la dirección ip (por ejemplo, 127.0.0.1, que representa tu propia máquina) y un puerto en esta máquina. Un puerto es un número entero entre 0 y 65535, preferiblemente mayor que 1024 (por ejemplo, 8080, 32254). Así, un socket puede tener la siguiente dirección: 127.0.0.1:32245.
>Nota: [aquí tienes más información sobre puertos](https://vermiip.es/puertos.php)

Para **iniciar la comunicación**, 
1. Un programa llamado **cliente crea un socket para solicitar una conexión** con otro programa llamado servidor. El cliente usa la dirección y el puerto del servidor para enviar una solicitud. 
2. El **servidor escucha en el puerto indicado, acepta o rechaza la conexión y, si la acepta, crea un socket para interactuar con el cliente**. 

Tanto el cliente como el servidor pueden intercambiar datos a través de sus sockets. Por lo general, un servidor interactúa con múltiples clientes simultáneamente.

>**Nota:** Tanto los programas del cliente como del servidor pueden ejecutarse en la misma computadora o en diferentes máquinas conectadas a través de una red.

## Sockets en Java

La biblioteca estándar de Java proporciona dos clases principales para la interacción entre programas mediante sockets:

- `Socket`: Representa un lado de una conexión bidireccional (usado por clientes y servidores).

- `ServerSocket`: Representa un tipo especial de socket que escucha y acepta conexiones de clientes (solo utilizado por servidores).

Ambas clases están ubicadas en el paquete `java.net.*`.

### Código del Servidor

Para crear un servidor en Java, usamos `ServerSocket`:
````java
import java.io.*;
import java.net.*;

public class EchoServer {
private static final int PORT = 34522;

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                try (
                    Socket socket = server.accept();
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                ) {
                    String msg = input.readUTF();
                    output.writeUTF(msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
````
Este código crea un `ServerSocket` que escucha en el puerto 34522 y acepta clientes en un bucle infinito. Cada cliente que se conecta envía un mensaje y el servidor lo devuelve.

### Código del Cliente

El cliente necesita conocer la dirección IP y el puerto del servidor para conectarse:
````java
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class EchoClient {
private static final String SERVER_ADDRESS = "127.0.0.1";
private static final int SERVER_PORT = 34522;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            Scanner scanner = new Scanner(System.in);
            String msg = scanner.nextLine();
            
            output.writeUTF(msg);
            String receivedMsg = input.readUTF();
            System.out.println("Recibido del servidor: " + receivedMsg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
````
## Conclusión

Java proporciona las clases `Socket` y `ServerSocket` para la comunicación en red. Un cliente inicia la conexión y un servidor la acepta. Ambos pueden intercambiar datos usando `DataInputStream` y `DataOutputStream`. Para manejar múltiples clientes simultáneamente, se recomienda usar un servidor multihilo. Con este conocimiento, puedes desarrollar aplicaciones de chat o almacenamiento de archivos en red.

