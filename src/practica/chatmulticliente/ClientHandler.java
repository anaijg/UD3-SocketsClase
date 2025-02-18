package practica.chatmulticliente;
 // Paquete donde se encuentra la clase

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;

// Clase que maneja la comunicación con un cliente individual
public class ClientHandler extends Thread {
    private final Socket socket; // Socket del cliente
    private final Set<ClientHandler> clientes; // Referencia a la lista de clientes
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
