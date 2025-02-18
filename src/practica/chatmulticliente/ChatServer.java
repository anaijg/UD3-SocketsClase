package practica.chatmulticliente;
 // Paquete donde se encuentra la clase

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

// Clase principal que actúa como servidor del chat
public class ChatServer {
    public static final int PORT = 12345; // Puerto donde el servidor escuchará conexiones
    private static final Set<ClientHandler> clientes = new HashSet<>(); // Lista de clientes conectados

    public static void main(String[] args) {
        // Se inicia el servidor y se espera por conexiones entrantes
        try (ServerSocket servidor = new ServerSocket(PORT)) {
            System.out.println("Servidor de chat en línea en el puerto " + PORT);

            while (true) { // Bucle infinito para aceptar conexiones de clientes
                Socket socket = servidor.accept(); // Espera a que un cliente se conecte
                System.out.println("Nuevo cliente conectado.");

                // Crea un manejador para el nuevo cliente y lo ejecuta en un hilo separado
                ClientHandler nuevoCliente = new ClientHandler(socket, clientes);
                synchronized (clientes) { // Bloquea la lista de clientes para evitar problemas de concurrencia
                    clientes.add(nuevoCliente);
                }
                nuevoCliente.start(); // Inicia el hilo para gestionar al cliente
            }
        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para enviar un mensaje a todos los clientes conectados
    public static void broadcast(String mensaje, ClientHandler remitente, Set<ClientHandler> clientes) {
        synchronized (clientes) { // Bloqueo para evitar modificaciones simultáneas
            for (ClientHandler cliente : clientes) {
                if (cliente != remitente) { // No enviamos el mensaje al remitente
                    cliente.enviarMensaje(mensaje);
                }
            }
        }
    }
}
